import com.avast.gradle.dockercompose.ComposeExtension

plugins {
    id("com.avast.gradle.docker-compose") version "0.16.9"
}

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":client-commons"))
    implementation("io.github.openfeign:feign-jaxb")
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

dockerCompose.isRequiredBy(tasks["test"])
configure<ComposeExtension> {
    useComposeFiles.add("${buildDir}${File.separator}docker${File.separator}docker-compose.yml")
    waitForTcpPorts.set(true)
    captureContainersOutput.set(true)
    captureContainersOutputToFiles.set(buildDir.resolve("docker_logs"))
    environment.putAll(
        mapOf(
            "TEAMCITY_VERSION" to "2021.1",
            "DOCKER_REGISTRY" to project.properties["docker.registry"]
        )
    )
    stopContainers.set(!project.hasProperty("test.container.leave"))
}

tasks.processTestResources{
    dependsOn("copyDockerFiles")
}

tasks.register<Sync>("copyDockerFiles"){
    from("${projectDir}${File.separator}docker")
    into("${buildDir}${File.separator}docker")
}

tasks.withType<Test> {
    dependsOn("composeUp")
}

