package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityBranches(
    @JsonProperty("branch")
    val branches: List<TeamcityBranch> = ArrayList<TeamcityBranch>(),
)
