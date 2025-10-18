package org.octopusden.octopus.infrastructure.jira.dto

data class MoveIssuesToSprintRequest(
    val issues: List<String>
)