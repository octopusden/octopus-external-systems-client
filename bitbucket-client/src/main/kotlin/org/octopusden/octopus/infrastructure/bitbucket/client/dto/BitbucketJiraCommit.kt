package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class BitbucketJiraCommit(
    val toCommit: BitbucketCommit,
    val id: String = toCommit.id,
    val repository: BitbucketRepository
)
