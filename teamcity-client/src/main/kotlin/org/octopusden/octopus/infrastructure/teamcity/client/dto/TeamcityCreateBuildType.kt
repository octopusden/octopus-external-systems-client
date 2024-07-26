package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
data class TeamcityCreateBuildType(
    val id: String? = null,
    val name: String,
    val projectId: String? = null,
    val projectName: String? = null,
    val template: TeamcityLinkBuildType? = null,
    @JsonProperty("vcs-root-entries")
    val vcsRoots: TeamcityVcsRootEntries? = null,
    val parameters: TeamcityProperties? = null,

    val templateFlag: Boolean? = null,
    val project: TeamcityLinkProject,
    val templates: TeamcityBuildTypes? = null,
    val settings: TeamcityProperties? = null,
    val steps: TeamcitySteps? = null,
    val features: TeamcityFeatures? = null,
    val triggers: TeamcityTriggers? = null,
    @JsonProperty("snapshot-dependencies")
    val snapshotDependencies: TeamcitySnapshotDependencies? = null,
)
