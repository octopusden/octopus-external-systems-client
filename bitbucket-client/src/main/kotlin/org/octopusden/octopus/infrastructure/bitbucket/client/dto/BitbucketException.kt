package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BitbucketException @JsonCreator constructor(
    val message: String,
    val exceptionName: BitbucketExceptionName?
)
