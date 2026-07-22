package org.octopusden.octopus.infrastructure.confluence.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConfluenceSearchResponse(
    val results: List<ConfluencePage> = emptyList()
)
