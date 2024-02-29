import com.avast.gradle.dockercompose.ComposeExtension

plugins {
    id("com.avast.gradle.docker-compose") version "0.16.9"
}

java {
    withJavadocJar()
    withSourcesJar()
}

if ((project.properties["gitlab.skip"] as String).toBoolean()) {
    tasks["test"].enabled = false
} else {
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
}

dependencies {
    api(project(":test-client-commons"))
    implementation("org.gitlab4j:gitlab4j-api:5.3.0")
    testImplementation(project(":test-client-test-commons"))
}
