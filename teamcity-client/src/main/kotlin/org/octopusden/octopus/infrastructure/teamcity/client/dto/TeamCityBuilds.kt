package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityBuilds(
    @JsonProperty("build")
    var builds: List<TeamcityBuild> = ArrayList<TeamcityBuild>()
)