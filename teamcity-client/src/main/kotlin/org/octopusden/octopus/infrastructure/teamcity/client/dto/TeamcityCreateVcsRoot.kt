package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityCreateVcsRoot(
    val name: String,
    val vcsName: String,
    val projectLocator: String,
    val properties: TeamcityProperties,
)
