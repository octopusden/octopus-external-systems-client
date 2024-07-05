package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamcityCreateProject(
    val name: String,
//    val sourceProjectLocator: String? = null,
    val id: String,
//    val copyAllAssociatedSettings: Boolean? = null,
//    val projectsIdsMap: TeamcityProperties? = null,
//    val buildTypesIdsMap: TeamcityProperties? = null,
//    val vcsRootsIdsMap: TeamcityProperties? = null,
//    val sourceProject: TeamcityProject? = null,
    val parentProject: TeamcityLinkProject? = null,
)
