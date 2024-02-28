import com.avast.gradle.dockercompose.ComposeExtension

plugins {
    id("com.avast.gradle.docker-compose") version "0.16.9"
}

java {
    withJavadocJar()
    withSourcesJar()
}

if ((project.properties["bitbucket.license"] as String).isBlank()) {
    tasks["test"].enabled = false
} else {
    configure<ComposeExtension> {
        useComposeFiles.add("${projectDir}${File.separator}docker${File.separator}docker-compose.yml")
        waitForTcpPorts.set(true)
        captureContainersOutputToFiles.set(buildDir.resolve("docker_logs"))
        environment.putAll(
            mapOf(
                "BITBUCKET_LICENSE" to project.properties["bitbucket.license"],
                "DOCKER_REGISTRY" to project.properties["docker.registry"]
            )
        )
    }
    dockerCompose.isRequiredBy(tasks["test"])
}

dependencies {
    api(project(":test-client-commons"))
    implementation(project(":bitbucket-client"))
    testImplementation(project(":test-client-test-commons"))
}
