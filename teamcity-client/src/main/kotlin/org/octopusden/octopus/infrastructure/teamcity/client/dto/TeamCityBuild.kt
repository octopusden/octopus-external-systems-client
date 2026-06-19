package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityBuild(
    val id: String,
    val buildTypeId: String? = null,
    val buildType: TeamcityBuildType? = null,
    val number: String? = null,
    val status: String? = null,
    val state: String? = null,
    val branchName: String? = null,
    val defaultBranch: Boolean? = null,
    val href: String? = null,
    val webUrl: String? = null,
    val finishDate: String? = null,
    val lastChanges: TeamcityChanges? = null,
    @JsonProperty("snapshot-dependencies")
    val snapshotDependencies: TeamcityBuildSnapshotDependencies? = null
)
