pluginManagement {
    val releaseManagementVersion = extra["release-management.version"] as String
    val kotlinVersion: String = extra["kotlin.version"] as String

    plugins {
        id("org.octopusden.release-management") version (releaseManagementVersion)
        id("org.jetbrains.kotlin.jvm") version (kotlinVersion)
    }
}

rootProject.name = "bitbucket-client"

include("bitbucket-test-client")
include("bitbucket-client")
include("sonarqube-client")
include("client-commons")
