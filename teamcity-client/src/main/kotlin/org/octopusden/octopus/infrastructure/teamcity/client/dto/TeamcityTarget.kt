package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityTarget(
    val anyProblem: Boolean? = null,
    val problem: TeamcityProblem? = null,
    val test: TeamcityTest? = null
)
