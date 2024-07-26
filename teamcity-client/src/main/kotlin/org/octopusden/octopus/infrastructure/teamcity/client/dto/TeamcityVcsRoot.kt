package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityVcsRoot(
    val id: String,
    val name: String,
    val vcsName: String? = null,
    val href: String,
    val project: TeamcityProject? = null,
    val projectLocator: String? = null,
    val properties: TeamcityProperties? = null,
)
