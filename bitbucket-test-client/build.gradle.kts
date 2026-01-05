import com.avast.gradle.dockercompose.ComposeExtension
import java.util.Base64

plugins {
    id("com.avast.gradle.docker-compose") version "0.16.9"
    id("org.octopusden.octopus.oc-template")
}

java {
    withJavadocJar()
    withSourcesJar()
}

fun String.getExt() = project.ext[this] as String

val commonOkdParameters = mapOf(
    "ACTIVE_DEADLINE_SECONDS" to "okdActiveDeadlineSeconds".getExt(),
    "DOCKER_REGISTRY" to "dockerRegistry".getExt()
)

configure<ComposeExtension> {
    useComposeFiles.add("${projectDir}${File.separator}docker${File.separator}docker-compose.yml")
    waitForTcpPorts.set(true)
    captureContainersOutputToFiles.set(layout.buildDirectory.dir("docker-logs"))
    environment.putAll(
        mapOf(
            "BITBUCKET_LICENSE" to project.properties["bitbucket.license"],
            "BITBUCKET_IMAGE_TAG" to project.properties["bitbucket.image-tag"],
            "POSTGRES_IMAGE_TAG" to project.properties["postgres.image-tag"],
            "DOCKER_REGISTRY" to project.properties["docker.registry"]
        )
    )
}

ocTemplate {
    workDir.set(layout.buildDirectory.dir("okd"))

    clusterDomain.set("okdClusterDomain".getExt())
    namespace.set("okdProject".getExt())
    prefix.set("ext-clients")
    attempts.set(25)

    "okdWebConsoleUrl".getExt().takeIf { it.isNotBlank() }?.let{
        webConsoleUrl.set(it)
    }

    group("bitbucketServices").apply {
        service("bitbucket-db") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/postgres.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "POSTGRES_IMAGE_TAG" to properties["postgres.image-tag"] as String
            ))
        }
        service("bitbucket") {
            templateFile.set(rootProject.layout.projectDirectory.file("okd/bitbucket.yaml"))
            parameters.set(commonOkdParameters + mapOf(
                "BITBUCKET_LICENSE" to Base64.getEncoder().encodeToString("bitbucketLicense".getExt().toByteArray()),
                "BITBUCKET_IMAGE_TAG" to properties["bitbucket.image-tag"] as String
            ))
            dependsOn.set(listOf("bitbucket-db"))
        }
    }
}

if ((project.properties["bitbucket.license"] as String).isBlank()) {
    tasks["test"].enabled = false
} else {
    tasks.withType<Test> {
        when ("testPlatform".getExt()) {
            "okd" -> {
                systemProperties["test.bitbucket-host"] = ocTemplate.getOkdHost("bitbucket")
                ocTemplate.isRequiredBy(this)
            }
            "docker" -> {
                systemProperties["test.bitbucket-host"] = "localhost:7990"
                dockerCompose.isRequiredBy(this)
            }
        }
    }
}

dependencies {
    api(project(":test-client-commons"))
    implementation(project(":bitbucket-client"))
    testImplementation(project(":test-client-test-commons"))
}
