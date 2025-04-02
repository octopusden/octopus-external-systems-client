package org.octopusden.octopus.infrastructure.teamcity.client.dto

/**
 * Represents a requirement to agent parameters.
 */
data class TeamcityAgentRequirement(
    val id: String?,
    val name: String?,
    val type: String,
    val disabled: Boolean?,
    val inherited: Boolean?,
    val href: String?,
    val properties: TeamcityProperties
)
