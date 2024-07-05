package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TeamcityBuildType(
    val id: String,
    val name: String,
    val projectId: String,
    val projectName: String,
//    val template: TeamcityBuildType,
    val href: String,
    @JsonProperty("vcs-root-entries")
    val vcsRoots: TeamcityVcsRootEntries? = null,
    val parameters: TeamcityProperties? = null,

    val webUrl: String,
//    val paused: Boolean,

//    val internalId: String,
//    val templateFlag: Boolean,
//    val type: Type,
//    val uuid: String,
//    val description: String,
//    val projectInternalId: String,
//    val inherited: Boolean,
//    val links: Links,
    val project: TeamcityProject? = null,
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
//    val builds: Builds,
//    val investigations: Investigations,
//    val compatibleAgents: Agents,
//    val compatibleCloudImages: CloudImages,
//    val vcsRootInstances: VcsRootInstances,
//    val externalStatusAllowed: Boolean,
//    val pauseComment: Comment,
//    val locator: String,
)
//{
//    enum class Type(val value: String){REGULAR("regular"), COMPOSITE("composite"), DEPLOYMENT("deployment")}
//}

