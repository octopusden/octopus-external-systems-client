package org.octopusden.octopus.infrastructure.teamcity.client.dto

import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildTypeLocator

data class TeamcityCreateQueuedBuild(
    val buildType: BuildTypeLocator,
    val branchName: String,
    val comment: TeamcityBuildComment? = null,
    val properties: TeamcityProperties? = null
)