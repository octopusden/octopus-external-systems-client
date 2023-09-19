java {
    withJavadocJar()
    withSourcesJar()
}
repositories {
    mavenCentral()
}

dependencies {
    api(project(":client-commons"))
}
