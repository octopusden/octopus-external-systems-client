package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BitbucketCommitChange(
    val contentId: String,
    val path: BitbucketCommitChangePath,
    val type: BitbucketCommitChangeType
) : BaseBitbucketEntity<String>(contentId) {
    data class BitbucketCommitChangePath(
        @JsonProperty("toString")
        val value: String
    )
    enum class BitbucketCommitChangeType {
        ADD, MODIFY, DELETE
    }
}
