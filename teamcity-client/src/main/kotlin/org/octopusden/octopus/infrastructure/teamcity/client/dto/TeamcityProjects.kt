package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityProjects(
    val count: Int? = null,
    val href: String? = null,
    val nextHref: String? = null,
    val prevHref: String? = null,
    @JsonProperty("project")
    var projects: List<TeamcityProject> = ArrayList<TeamcityProject>()
)
