package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityRevision(
    val version: String? = null,
    val vcsBranchName: String? = null,
    @JsonProperty("vcs-root-instance")
    val vcsRootInstance: TeamcityVcsRootInstance? = null
)
