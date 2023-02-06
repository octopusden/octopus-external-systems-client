package org.octopusden.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class BitbucketAuthor @JsonCreator constructor(
    val name: String
)
