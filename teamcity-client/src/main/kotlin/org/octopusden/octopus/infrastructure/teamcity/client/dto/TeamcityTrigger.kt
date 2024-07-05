package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityTrigger(
    val type: String,
    val disabled: Boolean,
    val properties: TeamcityProperties
)
