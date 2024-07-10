package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamcityVcsRoot(
    val id: String,
    val name: String,
    val vcsName: String? = null,
    val href: String,
    val project: TeamcityProject? = null,
    val projectLocator: String? = null,
    val properties: TeamcityProperties? = null,
)
