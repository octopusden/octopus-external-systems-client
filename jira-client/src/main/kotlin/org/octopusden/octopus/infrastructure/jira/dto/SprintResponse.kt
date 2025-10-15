package org.octopusden.octopus.infrastructure.jira.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.net.URI

@JsonIgnoreProperties(ignoreUnknown = true)
data class SprintResponse(
    val id: Long,
    val self: URI,
    val name: String,
    val startDate: String,
    val endDate: String,
    val activatedDate: String,
    val originBoardId: Long
)
