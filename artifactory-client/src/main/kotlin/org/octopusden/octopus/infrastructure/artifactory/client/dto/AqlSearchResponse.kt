package org.octopusden.octopus.infrastructure.artifactory.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AqlSearchResponse(
    val results: List<AqlItem> = emptyList(),
    val range: AqlRange? = null
)