package org.octopusden.octopus.infrastructure.teamcity.client.dto

data class TeamcityBuild(
    val id: String,
    val buildTypeId: String?,
    val number: String?,
    val status: String?,
    val state: String?,
    val branchName: String?,
    val defaultBranch: Boolean?,
    val href: String,
    val webUrl: String,
    val finishDate: String?
)
