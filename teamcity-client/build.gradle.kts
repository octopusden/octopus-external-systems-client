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
    prefix.set("external-clients")

    "okdWebConsoleUrl".getExt().takeIf { it.isNotBlank() }?.let{
        webConsoleUrl.set(it)
    }

    group("teamcityPVCs").apply {
        service("teamcity-2022-pvc") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-2022-pvc.yaml"))
        }
        service("teamcity-2025-pvc") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-2025-pvc.yaml"))
        }
    }

    group("teamcitySeedUploaders").apply {
        service("teamcity-2022-uploader") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-2022-uploader.yaml"))
            parameters.set(mapOf(
                "SERVICE_ACCOUNT_ANYUID" to project.properties["okd.service-account-anyuid"] as String,
                "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt()
            ))
        }
        service("teamcity-2025-uploader") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-2025-uploader.yaml"))
            parameters.set(mapOf(
                "SERVICE_ACCOUNT_ANYUID" to project.properties["okd.service-account-anyuid"] as String,
                "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt()
            ))
        }
    }

    group("teamcityServers").apply {
        service("teamcity-2022") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-2022.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "TEAMCITY_2022_IMAGE_TAG" to properties["teamcity-2022.image-tag"] as String
            ))
        }
        service("teamcity-2025") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/teamcity-2025.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "TEAMCITY_2025_IMAGE_TAG" to project.properties["teamcity-2025.image-tag"] as String
            ))
        }
    }
}

val copyFilesTeamcity2022 = tasks.register<Exec>("copyFilesTeamcity2022") {
    dependsOn("ocCreateTeamcityPVCs", "ocCreateTeamcitySeedUploaders")
    val localFile = layout.projectDirectory.dir("docker/data.zip").asFile.absolutePath
    commandLine("oc", "cp", localFile, "-n", "okdProject".getExt(),
        "${ocTemplate.getPod("teamcity-2022-uploader")}:/seed/seed.zip")
}

val copyFilesTeamcity2025 = tasks.register<Exec>("copyFilesTeamcity2025") {
    dependsOn("ocCreateTeamcityPVCs", "ocCreateTeamcitySeedUploaders")
    val localFile = layout.projectDirectory.dir("docker/dataV25.zip").asFile.absolutePath
    commandLine("oc", "cp", localFile, "-n", "okdProject".getExt(),
        "${ocTemplate.getPod("teamcity-2025-uploader")}:/seed/seed.zip")
}

val seedTeamcity = tasks.register("seedTeamcity") {
    dependsOn(copyFilesTeamcity2022, copyFilesTeamcity2025)
    finalizedBy("ocDeleteTeamcitySeedUploaders")
}

when ("testPlatform".getExt()) {
    "okd" -> {
        tasks.withType<Test> {
            systemProperties["test.teamcity-2022-host"] = ocTemplate.getOkdHost("teamcity-2022")
            systemProperties["test.teamcity-2025-host"] = ocTemplate.getOkdHost("teamcity-2025")
            dependsOn(seedTeamcity)
            dependsOn("ocCreateTeamcityServers")
            finalizedBy("ocDeleteTeamcityServers", "ocDeleteTeamcityPVCs")
        }
        tasks.named("ocCreateTeamcityServers").configure {
            mustRunAfter(seedTeamcity)
            mustRunAfter("ocDeleteTeamcitySeedUploaders")
        }
    }
    "docker" -> {
        tasks.withType<Test> {
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