package org.octopusden.octopus.infrastructure.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
data class ActiveSprintResponse(
    val values: List<SprintResponse> = emptyList()
)