package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityRevisions(
    val count: Int? = null,
    @JsonProperty("revision")
    val revision: List<TeamcityRevision> = emptyList()
)
