package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityChanges(
    val count: Int? = null,
    val href: String? = null,
    @JsonProperty("change")
    val change: List<TeamcityChange> = emptyList(),
)
