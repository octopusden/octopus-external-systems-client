plugins {
    id("java")
}

group = "org.octopusden.octopus.octopus-external-systems-clients"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api(project(":client-commons"))
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.6.0.202305301015-r")
}
