package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityTriggers(
    val trigger: List<TeamcityTrigger> = ArrayList<TeamcityTrigger>()
)
