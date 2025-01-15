package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class BitbucketRepository @JsonCreator constructor(
    val id: Long,
    val name: String,
    val slug: String,
    val links: BitbucketLinks,
    val project: BitbucketProject
)
