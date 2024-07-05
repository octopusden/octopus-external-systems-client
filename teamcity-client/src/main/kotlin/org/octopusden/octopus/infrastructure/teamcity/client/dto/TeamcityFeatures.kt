package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityFeatures(
    @JsonProperty("feature")
    val features: List<TeamcityFeature> = ArrayList<TeamcityFeature>()
)
