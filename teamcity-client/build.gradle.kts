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

java.targetCompatibility = JavaVersion.VERSION_1_8

dependencies {
    api(project(":client-commons"))
    testImplementation("org.slf4j:slf4j-simple:2.0.7")
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

configure<ComposeExtension> {
    useComposeFiles.add("${buildDir}${File.separator}docker${File.separator}docker-compose.yml")
    waitForTcpPorts.set(true)
    captureContainersOutputToFiles.set(buildDir.resolve("docker_logs"))
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
