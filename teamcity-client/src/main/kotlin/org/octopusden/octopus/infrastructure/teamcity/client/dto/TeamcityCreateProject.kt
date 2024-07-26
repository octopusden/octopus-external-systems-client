package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityCreateProject(
    val name: String,
    val id: String? = null,
    val parentProject: TeamcityLinkProject? = null,
)
