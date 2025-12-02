import com.avast.gradle.dockercompose.ComposeExtension

plugins {
    java
    id("com.avast.gradle.docker-compose") version "0.16.9"
    id("org.octopusden.octopus.oc-template")
}

java {
    withJavadocJar()
    withSourcesJar()
}

configure<ComposeExtension> {
    useComposeFiles.add("${projectDir}${File.separator}docker${File.separator}docker-compose.yml")
    waitForTcpPorts.set(true)
    captureContainersOutputToFiles.set(layout.buildDirectory.file("docker_logs").get().asFile)
    environment.putAll(
        mapOf(
            "DOCKER_REGISTRY" to project.properties["docker.registry"],
            "GITEA_IMAGE_TAG" to properties["gitea.image-tag"]
        )
    )
}

fun String.getExt() = project.ext[this] as String

val commonOkdParameters = mapOf(
    "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
    "DOCKER_REGISTRY" to "dockerRegistry".getExt()
)

ocTemplate {
    workDir.set(layout.buildDirectory.dir("okd"))

    clusterDomain.set("okdClusterDomain".getExt())
    namespace.set("okdProject".getExt())
    prefix.set("ext-clients")

    "okdWebConsoleUrl".getExt().takeIf { it.isNotBlank() }?.let{
        webConsoleUrl.set(it)
    }

    group("giteaServices").apply {
        service("gitea-1") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/gitea.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "GITEA_IMAGE_TAG" to properties["gitea.image-tag"] as String,
                "GITEA_ID" to "1"
            ))
        }

        service("gitea-2") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/gitea.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "GITEA_IMAGE_TAG" to properties["gitea.image-tag"] as String,
                "GITEA_ID" to "2"
            ))
        }
    }
}

tasks.withType<Test> {
    when ("testPlatform".getExt()) {
        "okd" -> {
            systemProperties["test.gitea-host"] = ocTemplate.getOkdHost("gitea-1")
            systemProperties["test.additionally-gitea-host"] = ocTemplate.getOkdHost("gitea-2")
            ocTemplate.isRequiredBy(this)
        }
        "docker" -> {
            systemProperties["test.gitea-host"] = "localhost:3000"
            dockerCompose.isRequiredBy(this)
        }
    }
}

tasks["composeUp"].doLast {
    exec {
        setCommandLine("docker", "exec", "gitea-test-client-ft-gitea", "/script/add_admin.sh")
    }.assertNormalExitValue()
}

dependencies {
    api(project(":test-client-commons"))
    implementation(project(":gitea-client"))
    testImplementation(project(":test-client-test-commons"))
}


