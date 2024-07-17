package org.octopusden.octopus.infrastructure.teamcity.client

enum class TeamcityVCSType(private val value: String) {
    GIT("jetbrains.git");

    override fun toString() = value
}