package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamcityProject(
    val id: String,
    val name: String,
//    val internalId: String? = null,
//    val uuid: String? = null,
    val parentProjectId: String,
//    val parentProjectInternalId: String? = null,
//    val parentProjectName: String? = null,
//    val archived: Boolean? = null,
//    val virtual: Boolean? = null,
//    val description: String? = null,
    val href: String,
    val webUrl: String,
//    val links: Links? = null,
    val parentProject: TeamcityProject? = null,
//    val readOnlyUI: StateField? = null,
//    val defaultTemplate: TeamcityBuildType? = null,
//    val buildTypes: TeamcityBuildTypes? = null,
//    val templates: TeamcityBuildTypes? = null,
//    val parameters: TeamcityProperties? = null,
//    val vcsRoots: TeamcityVcsRoots? = null,
//    val projectFeatures: TeamcityProjectFeatures? = null,
//    val projects: TeamcityProjects? = null,
//    val cloudProfiles: CloudProfiles? = null,
//    val ancestorProjects: TeamcityProjects? = null,
//    val locator: String? = null
)
