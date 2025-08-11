package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityQueuedBuild(
    val id: String,
    val state: String
)