package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityStep(
    val id: String,
    val name: String,
    val type: String,
    val disabled: Boolean? = null,
    val properties: TeamcityProperties,
)
