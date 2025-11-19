package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityInvestigations(
    val investigation: List<TeamcityInvestigation> = emptyList()
)
