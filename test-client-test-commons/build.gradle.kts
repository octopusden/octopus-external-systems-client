java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(project(":test-client-commons"))
    api("org.junit.jupiter:junit-jupiter-engine:${project.properties["junit-jupiter.version"]}")
    api("org.junit.jupiter:junit-jupiter-params:${project.properties["junit-jupiter.version"]}")
    api("ch.qos.logback:logback-core:1.2.3")
    api("ch.qos.logback:logback-classic:1.2.3")
    api("org.slf4j:slf4j-api:1.7.30")
    api("org.apache.commons:commons-lang3:3.1")
}
