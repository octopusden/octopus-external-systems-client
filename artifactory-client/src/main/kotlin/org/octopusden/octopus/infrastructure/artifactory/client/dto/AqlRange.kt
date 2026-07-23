package org.octopusden.octopus.infrastructure.artifactory.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AqlRange(
    @field:JsonProperty("start_pos")
    val startPos: Int? = null,
    @field:JsonProperty("end_pos")
    val endPos: Int? = null,
    val total: Int? = null,
)
