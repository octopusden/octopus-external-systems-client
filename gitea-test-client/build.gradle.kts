import com.avast.gradle.dockercompose.ComposeExtension
import java.util.concurrent.TimeUnit

plugins {
    java
    id("com.avast.gradle.docker-compose") version "0.16.9"
}

java {
    withJavadocJar()
    withSourcesJar()
}

configure<ComposeExtension> {
    useComposeFiles.add("${projectDir}${File.separator}docker${File.separator}docker-compose.yml")
    waitForTcpPorts.set(true)
    captureContainersOutputToFiles.set(buildDir.resolve("docker_logs"))
    environment.putAll(
        mapOf(
            "DOCKER_REGISTRY" to project.properties["docker.registry"]
        )
    )
}

dockerCompose.isRequiredBy(tasks["test"])

tasks["composeUp"].doLast {
    logger.info("Create test-admin in Gitea")
    val process = ProcessBuilder(
        "docker", "exec", "gitea-test-client-ft-gitea",
        "/script/add_admin.sh"
    ).start()
    process.waitFor(10, TimeUnit.SECONDS)

    val output = process.inputStream.bufferedReader().readText()
    logger.info(output)

    val error = process.errorStream.bufferedReader().readText()
    if (error.isNotEmpty()) {
        throw GradleException(error)
    }
}

dependencies {
    api(project(":test-client-commons"))
    implementation(project(":gitea-client"))
    testImplementation(project(":test-client-test-commons"))
}


