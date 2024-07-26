package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityProjects(
    @JsonProperty("project")
    var projects: List<TeamcityProject> = ArrayList<TeamcityProject>()
)
