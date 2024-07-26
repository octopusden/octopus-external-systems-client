package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcitySteps(
    @JsonProperty("step")
    val steps: List<TeamcityStep> = ArrayList<TeamcityStep>()
)
