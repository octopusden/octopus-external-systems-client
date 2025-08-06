package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityProject(
    val id: String,
    val name: String,
    val parentProjectId: String? = null,
//    val parentProjectName: String? = null,
    val archived: Boolean? = null,
//    val virtual: Boolean? = null,
//    val description: String? = null,
    val href: String,
    val webUrl: String,
    val parentProject: TeamcityProject? = null,
//    val defaultTemplate: TeamcityBuildType? = null,
    val buildTypes: TeamcityBuildTypes? = null,
//    val templates: TeamcityBuildTypes? = null,
//    val parameters: TeamcityProperties? = null,
//    val vcsRoots: TeamcityVcsRoots? = null,
//    val projectFeatures: TeamcityProjectFeatures? = null,
    val projects: TeamcityProjects? = null
)
