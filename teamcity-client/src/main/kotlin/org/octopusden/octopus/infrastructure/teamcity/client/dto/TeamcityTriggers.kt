package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityTriggers(
    @JsonProperty("trigger")
    val triggers: List<TeamcityTrigger> = ArrayList<TeamcityTrigger>()
)
