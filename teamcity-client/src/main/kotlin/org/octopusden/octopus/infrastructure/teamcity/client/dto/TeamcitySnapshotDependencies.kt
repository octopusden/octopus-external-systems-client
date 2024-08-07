package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcitySnapshotDependencies(
    @JsonProperty("snapshot-dependency")
    val snapshotDependencies: List<TeamcitySnapshotDependency> = ArrayList<TeamcitySnapshotDependency>()
)
