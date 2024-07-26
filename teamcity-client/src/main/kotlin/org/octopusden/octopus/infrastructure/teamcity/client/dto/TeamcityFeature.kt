package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityFeature(
    val id: String,
    val type: String,
    val disabled: Boolean,
    val properties: TeamcityProperties,
)
