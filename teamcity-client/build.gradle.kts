java {
    withJavadocJar()
    withSourcesJar()
}
repositories {
    mavenCentral()
}

dependencies {
    api(project(":client-commons"))
    testImplementation("junit:junit:4.13.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}




