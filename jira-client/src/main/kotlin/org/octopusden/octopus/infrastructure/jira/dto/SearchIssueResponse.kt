package org.octopusden.octopus.infrastructure.jira.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class SearchIssueResponse(
    val issues: List<SearchIssue>
)