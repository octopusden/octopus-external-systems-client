package org.octopusden.octopus.infrastructure.jira.dto

data class SearchIssue(
    val id: String,
    val key: String?,
    val self: String?,
    val fields: SearchIssueFields,
)