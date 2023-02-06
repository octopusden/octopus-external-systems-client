import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    idea
    id("org.octopusden.release-management")
    id("org.jetbrains.kotlin.jvm")
}

allprojects {
    group = "org.octopusden.infrastructure"
    version = "1.0-SNAPSHOT"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "idea")
    apply(plugin = "java")

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
