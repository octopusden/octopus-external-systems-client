package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty


data class TeamcityBuildTypes(
    @JsonProperty("buildType")
    var buildTypes: List<TeamcityBuildType> = ArrayList<TeamcityBuildType>()
)
