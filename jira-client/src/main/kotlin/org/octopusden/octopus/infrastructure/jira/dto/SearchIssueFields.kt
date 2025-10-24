package org.octopusden.octopus.infrastructure.jira.dto

class SearchIssueFields (
    val summary: String?,
    val status: IssueStatus?,
    val assignee: Assignee?,
    val created: String?,
    val issueType: IssueType?,
    val issuePriority: IssuePriority?,
    val resolution: IssueResolution?
)