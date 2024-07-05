package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TeamcityProperties(
    @JsonProperty("property")
    var properties: List<TeamcityProperty> = ArrayList<TeamcityProperty>()
)
