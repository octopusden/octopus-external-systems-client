package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BitbucketEntityString @JsonCreator constructor(
    val values: List<String>,
    val size: Int,
    val isLastPage: Boolean,
    val start: Int,
    val limit: Int,
    val nextPageStart: Int?
)
