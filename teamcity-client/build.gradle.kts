import com.avast.gradle.dockercompose.ComposeExtension

plugins {
    id("com.avast.gradle.docker-compose") version "0.16.9"
    id("org.octopusden.octopus.oc-template")
}

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

java.targetCompatibility = JavaVersion.VERSION_1_8

dependencies {
    api(project(":client-commons"))
    api("io.github.openfeign.form:feign-form:3.8.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.7")
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("it.skrape:skrapeit:1.2.2")
}

configure<ComposeExtension> {
    useComposeFiles.add(layout.projectDirectory.file("docker/docker-compose.yml").asFile.path)
    waitForTcpPorts.set(true)
    captureContainersOutputToFiles.set(layout.buildDirectory.dir("docker-logs"))
    environment.putAll(
        mapOf(
            "DOCKER_REGISTRY" to project.properties["docker.registry"],
            "TEAMCITY_2022_IMAGE_TAG" to project.properties["teamcity-2022.image-tag"],
            "TEAMCITY_2025_IMAGE_TAG" to project.properties["teamcity-2025.image-tag"]
        )
    )
}

fun String.getExt() = project.ext[this] as String

val commonOkdParameters = mapOf(
    "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
    "DOCKER_REGISTRY" to "dockerRegistry".getExt(),
    "SERVICE_ACCOUNT_ANYUID" to project.properties["okd.service-account-anyuid"] as String
)

ocTemplate {
    workDir.set(layout.buildDirectory.dir("okd"))
    clusterDomain.set("okdClusterDomain".getExt())
    namespace.set("okdProject".getExt())
    prefix.set("ext-clients")

    "okdWebConsoleUrl".getExt().takeIf { it.isNotBlank() }?.let{
        webConsoleUrl.set(it)
    }

    group("teamcityPVCs").apply {
        service("teamcity22-pvc") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-pvc.yaml"))
            parameters.set(mapOf(
                "TEAMCITY_ID" to "22"
            ))
        }
        service("teamcity25-pvc") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-pvc.yaml"))
            parameters.set(mapOf(
                "TEAMCITY_ID" to "25"
            ))
        }
    }

    group("teamcitySeedUploaders").apply {
        service("teamcity22-uploader") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-uploader.yaml"))
            parameters.set(mapOf(
                "SERVICE_ACCOUNT_ANYUID" to project.properties["okd.service-account-anyuid"] as String,
                "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
                "TEAMCITY_ID" to "22"
            ))
        }
        service("teamcity25-uploader") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-uploader.yaml"))
            parameters.set(mapOf(
                "SERVICE_ACCOUNT_ANYUID" to project.properties["okd.service-account-anyuid"] as String,
                "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
                "TEAMCITY_ID" to "25"
            ))
        }
    }

    group("teamcityServers").apply {
        service("teamcity22") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "TEAMCITY_IMAGE_TAG" to properties["teamcity-2022.image-tag"] as String,
                "TEAMCITY_ID" to "22"
            ))
        }
        service("teamcity25") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "TEAMCITY_IMAGE_TAG" to project.properties["teamcity-2025.image-tag"] as String,
                "TEAMCITY_ID" to "25"
            ))
        }
    }
}

val copyFilesTeamcity2022 = tasks.register<Exec>("copyFilesTeamcity2022") {
    dependsOn("ocCreateTeamcityPVCs", "ocCreateTeamcitySeedUploaders")
    val localFile = layout.projectDirectory.dir("docker/data.zip").asFile.absolutePath
    commandLine("oc", "cp", localFile, "-n", "okdProject".getExt(),
        "${ocTemplate.getPod("teamcity22-uploader")}:/seed/seed.zip")
}

val copyFilesTeamcity2025 = tasks.register<Exec>("copyFilesTeamcity2025") {
    dependsOn("ocCreateTeamcityPVCs", "ocCreateTeamcitySeedUploaders")
    val localFile = layout.projectDirectory.dir("docker/dataV25.zip").asFile.absolutePath
    commandLine("oc", "cp", localFile, "-n", "okdProject".getExt(),
        "${ocTemplate.getPod("teamcity25-uploader")}:/seed/seed.zip")
}

val seedTeamcity = tasks.register("seedTeamcity") {
    dependsOn(copyFilesTeamcity2022, copyFilesTeamcity2025)
    finalizedBy("ocLogsTeamcitySeedUploaders", "ocDeleteTeamcitySeedUploaders")
}

tasks.named("ocCreateTeamcityServers").configure {
    dependsOn(seedTeamcity)
}

tasks.withType<Test> {
    when ("testPlatform".getExt()) {
        "okd" -> {
            systemProperties["test.teamcity-2022-host"] = ocTemplate.getOkdHost("teamcity22")
            systemProperties["test.teamcity-2025-host"] = ocTemplate.getOkdHost("teamcity25")
            dependsOn("ocCreateTeamcityServers")
            finalizedBy("ocLogsTeamcityServers", "ocDeleteTeamcityServers", "ocDeleteTeamcityPVCs")
        }
        "docker" -> {
            systemProperties["test.teamcity-2022-host"] = "localhost:8111"
            systemProperties["test.teamcity-2025-host"] = "localhost:8112"
            dockerCompose.isRequiredBy(this)
        }
    }
}

tasks.named("composeUp") {
    dependsOn(prepareTeamcity2022Data)
    dependsOn(prepareTeamcity2025Data)
}

val prepareTeamcity2022Data = tasks.register<Sync>("prepareTeamcity2022Data") {
    from(zipTree(layout.projectDirectory.file("docker/data.zip")))
    into(layout.buildDirectory.dir("teamcity-server-2022"))
}

val prepareTeamcity2025Data = tasks.register<Sync>("prepareTeamcity2025Data") {
    from(zipTree(layout.projectDirectory.file("docker/dataV25.zip")))
    into(layout.buildDirectory.dir("teamcity-server-2025"))
}