package org.octopusden.octopus.infrastructure.jira.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class SearchIssueFields(
    val summary: String?,
    val status: IssueStatus?,
    val assignee: Assignee?,
    val created: String?,
    @JsonProperty("issuetype")
    val issueType: IssueType?,
    val priority: IssuePriority?,
    val resolution: IssueResolution?
)