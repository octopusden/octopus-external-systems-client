package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityProperties(
    @JsonProperty("property")
    var properties: List<TeamcityProperty> = ArrayList<TeamcityProperty>()
)
