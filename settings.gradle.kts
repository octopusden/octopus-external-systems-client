pluginManagement {
    val kotlinVersion: String = extra["kotlin.version"] as String
    val ocTemplateVersion = extra["octopus-oc-template.version"] as String

    plugins {
        id("org.octopusden.octopus.oc-template") version (ocTemplateVersion)
        id("org.jetbrains.kotlin.jvm") version (kotlinVersion)
        id("io.github.gradle-nexus.publish-plugin") version("1.1.0") apply(false)
        id("io.gitlab.arturbosch.detekt") version (extra["detekt.version"] as String)
        id("org.jlleitschuh.gradle.ktlint") version (extra["ktlint-gradle.version"] as String)
        id("org.octopusden.octopus-quality") version (extra["octopus-quality.version"] as String)
    }
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "octopus-external-systems-clients"

include("client-commons")
include("test-client-commons")
include("test-client-test-commons")
include("bitbucket-client")
include("sonarqube-client")
include("jira-client")
include("bitbucket-test-client")
include("gitea-client")
include("gitea-test-client")
include("teamcity-client")
include("artifactory-client")
include("confluence-client")