import com.avast.gradle.dockercompose.ComposeExtension

plugins {
    id("com.avast.gradle.docker-compose") version "0.16.9"
}

java {
    withJavadocJar()
    withSourcesJar()
}

configure<ComposeExtension> {
    useComposeFiles.add("${buildDir}${File.separator}docker${File.separator}docker-compose.yml")
    waitForTcpPorts.set(true)
    captureContainersOutputToFiles.set(buildDir.resolve("docker_logs"))
//    captureContainersOutput.set(true)
    environment.putAll(
        mapOf(
            "DOCKER_REGISTRY" to project.properties["docker.registry"],
            "TEAMCITY_VERSION" to "2021.1.4",
        )
    )
}

dockerCompose.isRequiredBy(tasks["test"])

tasks.processTestResources {
    dependsOn("copyDockerFiles", "copyTeamcityData")
}

tasks.register<Sync>("copyDockerFiles") {
    from("${projectDir}${File.separator}docker") {
        exclude("data.zip")
    }
    into("${buildDir}${File.separator}docker")
}

tasks.register<Copy>("copyTeamcityData") {
    dependsOn("copyDockerFiles")
    from(zipTree("${projectDir}${File.separator}docker${File.separator}data.zip"))
    into("${buildDir}${File.separator}docker")
}

tasks.withType<Test> {
    dependsOn("composeUp")
}

dependencies {
    api(project(":test-client-commons"))
    implementation(project(":teamcity-client"))
    testImplementation(project(":test-client-test-commons"))
}


