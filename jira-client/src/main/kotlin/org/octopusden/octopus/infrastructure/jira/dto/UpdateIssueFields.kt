package org.octopusden.octopus.infrastructure.jira.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class UpdateIssueFields(
    val project: Project? = null,
    val issuetype: IssueType? = null,
    val summary: String? = null,
    val description: String? = null,
    val assignee: Assignee? = null
) : IssueFields
