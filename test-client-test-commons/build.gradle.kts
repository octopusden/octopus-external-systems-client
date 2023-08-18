java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":test-client-commons"))
    api("org.junit.jupiter:junit-jupiter-engine:${project.properties["junit-jupiter.version"]}")
    api("org.junit.jupiter:junit-jupiter-params:${project.properties["junit-jupiter.version"]}")
}
