package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityVcsRootInstances(
    @JsonProperty("vcs-root-instance")
    val vcsRootInstances: List<TeamcityVcsRootInstance> = ArrayList<TeamcityVcsRootInstance>()
)
