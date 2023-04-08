package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class BitbucketBranch @JsonCreator constructor(
    id: String,
    val displayId: String,
    val latestCommit: String
) : BaseBitbucketEntity<String>(id)
