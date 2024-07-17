package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamcityCreateBuildType(
    val id: String,
    val name: String? = null,
    val projectId: String? = null,
    val projectName: String? = null,
    val template: TeamcityLinkBuildType? = null,
    @JsonProperty("vcs-root-entries")
    val vcsRoots: TeamcityVcsRootEntries? = null,
    val parameters: TeamcityProperties? = null,

    val templateFlag: Boolean? = null,
//    val type: Type,
//    val description: String,
    val project: TeamcityLinkProject? = null,
    val templates: TeamcityBuildTypes? = null,
    val settings: TeamcityProperties? = null,
    val steps: TeamcitySteps? = null,
    val features: TeamcityFeatures? = null,
    val triggers: TeamcityTriggers? = null,
    @JsonProperty("snapshot-dependencies")
    val snapshotDependencies: TeamcitySnapshotDependencies? = null,
//    @JsonProperty("artifact-dependencies")
//    val artifactDdependencies: TeamcityArtifactDependencies,
//    @JsonProperty("agent-requirements")
//    val agentRequirements: TeamcityAgentRequirements,
//    val branches: Branches,
//    val vcsRootInstances: VcsRootInstances,
)//{
//    enum class Type(val value: String){REGULAR("regular"), COMPOSITE("composite"), DEPLOYMENT("deployment")}
//}

