package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityProjectFeatures(
    @JsonProperty("projectFeature")
    val projectFeatures: List<TeamcityProjectFeature> = ArrayList<TeamcityProjectFeature>()
)
