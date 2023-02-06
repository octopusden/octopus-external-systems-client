package org.octopusden.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class BitbucketProject @JsonCreator constructor(
    id: Long,
    val key: String
) : BaseBitbucketEntity<Long>(id)
