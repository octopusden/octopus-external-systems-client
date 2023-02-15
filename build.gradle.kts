import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    idea
    id("org.octopusden.release-management")
    id("org.jetbrains.kotlin.jvm")
    id("io.github.gradle-nexus.publish-plugin")
    signing
    `maven-publish`
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("MAVEN_USERNAME"))
            password.set(System.getenv("MAVEN_PASSWORD"))
        }
    }
}

allprojects {
    group = "org.octopusden.infrastructure"
    version = "1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "signing")
    apply(plugin = "maven-publish")

    repositories {
        mavenCentral()
    }

    val gitUrl = "https://github.com/octopusden/octopus-external-systems-client.git"

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                pom {
                    name.set(project.name)
                    description.set("Octopus module: ${project.name}")
                    url.set(gitUrl)
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    scm {
                        url.set(gitUrl)
                        connection.set("scm:git://github.com/octopusden/octopus-external-systems-client.git")
                    }
                    developers {
                        developer {
                            id.set("octopus")
                            name.set("octopus")
                        }
                    }
                }
            }
        }
    }

    signing {
        val signingKey: String? by project
        val signingPassword: String? by project
        useInMemoryPgpKeys(signingKey, signingPassword)
        sign(publishing.publications["maven"])
    }

    idea.module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            suppressWarnings = true
            jvmTarget = "1.8"
        }
    }

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-stdlib")
    }
}
