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
            "DOCKER_REGISTRY" to project.properties["docker.registry"]
        )
    )
}

dockerCompose.isRequiredBy(tasks["test"])

tasks.register<Sync>("prepareTeamcityServerData") {
    from(zipTree(layout.projectDirectory.file("docker/data.zip")))
    into(layout.buildDirectory.dir("teamcity-server"))
}

tasks.register<Sync>("prepareTeamcityServerDataV25") {
    from(zipTree(layout.projectDirectory.file("docker/data.zip")))
    into(layout.buildDirectory.dir("teamcity-server-2025"))
}

tasks.named("composeUp") {
    dependsOn("prepareTeamcityServerData")
    dependsOn("prepareTeamcityServerDataV25")
}
