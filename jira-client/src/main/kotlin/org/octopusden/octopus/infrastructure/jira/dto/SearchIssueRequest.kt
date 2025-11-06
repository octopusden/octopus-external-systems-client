package org.octopusden.octopus.infrastructure.jira.dto

/**
 * Represents a Jira search request payload for querying issues using the Jira REST API.
 *
 * @property jql The JQL (Jira Query Language) string used to filter issues.
 * @property fields A list of field names to include in the response.
 * Supported fields:
 * - `summary`: The issue summary or title.
 * - `status`: The current status of the issue (e.g., Open, In Progress, Closed).
 * - `assignee`: The user assigned to the issue.
 * - `created`: The date and time when the issue was created.
 * - `issueType`: The type of the issue (e.g., Bug, Task, Story).
 * - `priority`: The priority level of the issue (e.g., High, Medium, Low).
 * - `resolution`: The final resolution of the issue (e.g., Fixed, Won’t Fix, Duplicate).
 * If `null`, Jira returns its default subset of fields.
 * @property maxResults The maximum number of issues to return.
 * If `null`, Jira uses its default limit.
 */
data class SearchIssueRequest(
    val jql: String,
    val fields: List<String> = listOf(
        "summary",
        "status",
        "assignee",
        "priority",
        "resolution",
        "issuetype",
        "created"
    ),
    val maxResults: Int? = null
)
