package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityVcsRoots(
    @JsonProperty("vcs-root")
    val vcsRoots: List<TeamcityVcsRoot> = ArrayList<TeamcityVcsRoot>()
)
