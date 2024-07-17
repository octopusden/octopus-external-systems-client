package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamcityVcsRootInstance(
    val id: String,
    val name: String,
    @JsonProperty("vcs-root-id")
    val vcsRootId: String,
    @JsonProperty("vcs-root")
    val vcsRoot: String,
    val vcsName: String? = null,
    val href: String,
    val project: TeamcityProject? = null,
    val properties: TeamcityProperties? = null,
)
