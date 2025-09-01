package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityVcsRootInstance(
    val id: String,
    val name: String,
    @JsonProperty("vcs-root-id")
    val vcsRootId: String,
    @JsonProperty("vcs-root")
    val vcsRoot: TeamcityVcsRoot? = null,
    val vcsName: String? = null,
    val href: String,
    val project: TeamcityProject? = null,
    val properties: TeamcityProperties? = null,
)
