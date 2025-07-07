package org.octopusden.octopus.infrastructure.teamcity.client

data class TeamcityTestConfiguration(
    val name: String,
    val host: String,
    val version: String
)