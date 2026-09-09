package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityBranch(
    val name: String? = null,
    val builds: TeamcityBuilds? = null,
)
