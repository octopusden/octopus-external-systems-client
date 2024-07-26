package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityLinkFeature(
    val id: String? = null,
    val type: String,
    val disabled: Boolean? = null,
    val properties: TeamcityProperties,
)
