
java {
    withJavadocJar()
    withSourcesJar()
}
repositories {
    mavenCentral()
}

dependencies {
    api(project(":client-commons"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}
