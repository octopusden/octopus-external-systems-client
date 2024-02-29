package org.octopusden.octopus.infrastructure.gitea.client.dto

data class GiteaCreatePullRequest(
    val title: String,
    val body: String,
    val head: String,
    val base: String,
    val assignees: Set<String>,
    val assignee: String?
)
