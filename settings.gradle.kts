pluginManagement {
    val releaseManagementVersion = extra["octopus-release-management.version"] as String
    val kotlinVersion: String = extra["kotlin.version"] as String

    plugins {
        id("org.octopusden.octopus-release-management") version (releaseManagementVersion)
        id("org.jetbrains.kotlin.jvm") version (kotlinVersion)
        id("io.github.gradle-nexus.publish-plugin") version("1.1.0") apply(false)
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
include("gitlab-test-client")
