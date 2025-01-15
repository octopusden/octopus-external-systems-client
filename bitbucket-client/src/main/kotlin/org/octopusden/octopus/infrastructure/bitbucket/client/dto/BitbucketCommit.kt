package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
class BitbucketCommit @JsonCreator constructor(
    val id: String,
    val message: String,
    val author: BitbucketUser,
    val authorTimestamp: Date,
    val parents: List<BitbucketParent>
)
