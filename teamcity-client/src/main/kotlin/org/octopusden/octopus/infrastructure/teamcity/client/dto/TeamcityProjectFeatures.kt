package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityProjectFeatures(
    val projectFeature: List<TeamcityProjectFeature> = ArrayList<TeamcityProjectFeature>()
)
