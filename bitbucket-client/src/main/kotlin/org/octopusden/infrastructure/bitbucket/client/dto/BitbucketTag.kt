package org.octopusden.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class BitbucketTag @JsonCreator constructor(
    id: String,
    val displayId: String,
    val latestCommit: String
) : BaseBitbucketEntity<String>(id)
