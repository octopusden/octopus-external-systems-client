package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityInvestigation(
    val state: String,
    val assignee: TeamcityAssignee? = null,
    val assignment: TeamcityAssignment? = null
)
