plugins {
    `maven-publish`
}

publishing {
    repositories {
        maven {

        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

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
