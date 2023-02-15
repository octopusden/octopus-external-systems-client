
java {
    withJavadocJar()
    withSourcesJar()
}

dependencies {
    api(platform("io.github.openfeign:feign-bom:12.1"))
    api("io.github.openfeign:feign-httpclient")
    api("io.github.openfeign:feign-jackson")
    api("io.github.openfeign:feign-slf4j")
    api("org.apache.httpcomponents:httpclient:4.5.13")

    api(platform("com.fasterxml.jackson:jackson-bom:2.14.0"))
    api("com.fasterxml.jackson.module:jackson-module-kotlin")
    api("com.fasterxml.jackson.core:jackson-databind")


    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
