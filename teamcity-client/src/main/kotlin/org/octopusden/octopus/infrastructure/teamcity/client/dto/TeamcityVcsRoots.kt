package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TeamcityVcsRoots(
    @JsonProperty("vcs-root")
    val vcsRoots: List<TeamcityVcsRoot> = ArrayList<TeamcityVcsRoot>()
)
