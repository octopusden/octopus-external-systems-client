package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TeamcityVcsRoot(
    val vcsName: String,
    val project: TeamcityProject,
    val properties: TeamcityProperties,
)
