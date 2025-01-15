package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class BitbucketCommitChange(
    val contentId: String,
    val id: String = contentId,
    val type: BitbucketCommitChangeType,
    val path: BitbucketCommitChangePath,
    val srcPath: BitbucketCommitChangePath?
) {
    data class BitbucketCommitChangePath(
        @JsonProperty("toString")
        val value: String
    )
    enum class BitbucketCommitChangeType {
        ADD, MODIFY, DELETE, COPY, MOVE
    }
}
