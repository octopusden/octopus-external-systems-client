package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityAddInvestigation(
    val state: String,
    val assignee: TeamcityAssignee,
    val assignment: TeamcityAssignment? = null,
    val scope: TeamcityScope,
    val target: TeamcityTarget,
    val resolution: TeamcityResolution
)
