package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityProjects(
    var project: List<TeamcityProject> = ArrayList<TeamcityProject>()
)
