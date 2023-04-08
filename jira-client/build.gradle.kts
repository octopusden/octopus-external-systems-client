plugins {
    `maven-publish`
}

java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":client-commons"))
}
