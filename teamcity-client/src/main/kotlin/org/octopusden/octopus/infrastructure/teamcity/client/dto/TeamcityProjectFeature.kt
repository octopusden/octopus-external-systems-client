package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityProjectFeature(
    val id: String? = null,
    val name: String? = null,
    val type: String? = null,
    val disabled: Boolean? = null,
    val inherited: Boolean? = null,
    val href: String? = null,
    val properties: TeamcityProperties? = null,
)
