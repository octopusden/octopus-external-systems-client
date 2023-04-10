package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.Date

@JsonIgnoreProperties(ignoreUnknown = true)
class BitbucketCommit @JsonCreator constructor(
    id: String,
    val message: String,
    val committer: BitbucketCommitter,
    val author: BitbucketAuthor,
    val authorTimestamp: Date,
    val parents: List<BitbucketParent>
) : BaseBitbucketEntity<String>(id)
