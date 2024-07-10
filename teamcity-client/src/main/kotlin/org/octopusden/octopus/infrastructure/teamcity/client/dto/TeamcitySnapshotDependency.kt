package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcitySnapshotDependency(
    val id: String,
    val type: String,
    val properties: TeamcityProperties,
    @JsonProperty("source-buildType")
    val sourceBuildType: TeamcityLinkBuildType,
)
