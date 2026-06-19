package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityBuildSnapshotDependencies(
    val count: Int? = null,
    @JsonProperty("build")
    val build: List<TeamcityBuild> = emptyList()
)
