package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityRoleAssignment(
    val roleId: String? = null,
    val scope: TeamcityRoleScope? = null,
    val href: String? = null
)
