package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityRecipeOverview(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val type: String? = null,
    val format: String? = null,
    val usages: List<TeamcityRecipeUsage> = emptyList(),
)

data class TeamcityRecipeUsage(
    val id: String,
    val name: String? = null,
    val projectId: String? = null,
    val type: String? = null,
)
