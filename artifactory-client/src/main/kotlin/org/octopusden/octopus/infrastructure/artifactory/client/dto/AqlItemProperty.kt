package org.octopusden.octopus.infrastructure.artifactory.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AqlItemProperty(
    val key: String? = null,
    val value: String? = null,
)
