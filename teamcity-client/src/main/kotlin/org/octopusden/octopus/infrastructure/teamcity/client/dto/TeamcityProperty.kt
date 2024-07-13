package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityProperty(
    val name: String,
    val value: String,
    val inherited: Boolean? = null,
    val type: Type? = null,
)

data class Type(val rawValue: String)
