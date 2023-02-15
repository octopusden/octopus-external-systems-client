import com.avast.gradle.dockercompose.ComposeExtension

plugins {
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
            "BITBUCKET_LICENSE" to project.properties["bitbucket.license"],
            "DOCKER_REGISTRY" to project.properties["docker.registry"]
        )
    )
}

dockerCompose.isRequiredBy(tasks["test"])

dependencies {
    implementation(project(":bitbucket-client"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r")

    testImplementation("org.junit.jupiter:junit-jupiter-engine:${project.properties["junit-jupiter.version"]}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${project.properties["junit-jupiter.version"]}")

    testImplementation("ch.qos.logback:logback-core:1.2.3")
    testImplementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("org.slf4j:slf4j-api:1.7.30")
}
