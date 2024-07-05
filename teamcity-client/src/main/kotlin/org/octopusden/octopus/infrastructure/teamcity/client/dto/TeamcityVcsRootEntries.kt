package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.*

//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TeamcityVcsRootEntries(
    @JsonProperty("vcs-root-entry")
    val entries: List<TeamcityVcsRootEntry> = LinkedList<TeamcityVcsRootEntry>()
)
