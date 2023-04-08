package org.octopusden.octopus.infrastructure.jira.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class IssueFields(
    val project: Project,
    val issuetype: IssueType,
    val summary: String,
    val description: String,
    val assignee: Assignee? = null
)
