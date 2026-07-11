package org.octopusden.octopus.infrastructure.artifactory.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class AqlItem(
    val repo: String? = null,
    val path: String? = null,
    val name: String? = null,
    val type: String? = null,
    val size: Long? = null,
    val created: String? = null,
    val modified: String? = null,
    val updated: String? = null,
    val properties: List<AqlItemProperty>? = null,
    @field:JsonProperty("created_by")
    val createdBy: String? = null,
    @field:JsonProperty("modified_by")
    val modifiedBy: String? = null,
)
