package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class TeamcityVcsRootEntries(
    @JsonProperty("vcs-root-entry")
    val entries: List<TeamcityVcsRootEntry> = LinkedList<TeamcityVcsRootEntry>()
)
