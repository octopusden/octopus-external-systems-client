package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcitySteps(
    val step: List<TeamcityStep>  = ArrayList<TeamcityStep>()
)
