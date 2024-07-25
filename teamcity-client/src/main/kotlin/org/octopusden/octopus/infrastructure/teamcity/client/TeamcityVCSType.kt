package org.octopusden.octopus.infrastructure.teamcity.client

enum class TeamcityVCSType(private val value: String) {
    GIT("jetbrains.git"),
    HG("mercurial");

    override fun toString() = value
}