package org.octopusden.octopus.infrastructure.teamcity.client.dto


data class TeamcityBuildTypes(
    var buildType: List<TeamcityBuildType> = ArrayList<TeamcityBuildType>()
)
