package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamcityBuildType(
    val id: String,
    val name: String,
    val projectId: String,
    val projectName: String,
    val href: String,
    @JsonProperty("vcs-root-entries")
    val vcsRoots: TeamcityVcsRootEntries? = null,
    val parameters: TeamcityProperties? = null,

    val webUrl: String? = null,

    val templateFlag: Boolean? = null,
    val project: TeamcityProject? = null,
    val templates: TeamcityBuildTypes? = null,
    val settings: TeamcityProperties? = null,
    val steps: TeamcitySteps? = null,
    val features: TeamcityFeatures? = null,
    val triggers: TeamcityTriggers? = null,
    @JsonProperty("snapshot-dependencies")
    val snapshotDependencies: TeamcitySnapshotDependencies? = null,
)

