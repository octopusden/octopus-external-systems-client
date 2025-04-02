package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Represents a list of AgentRequirement entities.
 */
data class TeamcityAgentRequirements (
    val count: Int,
    @JsonProperty("agent-requirement")
    val agentRequirements: List<TeamcityAgentRequirement> = ArrayList()
)