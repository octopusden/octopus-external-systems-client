package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import java.util.Date

class BitbucketPullRequest(
    id: Long,
    val title: String,
    val description: String? = null,
    val author: BitbucketPullRequestUser,
    val reviewers: List<BitbucketPullRequestUser>,
    val fromRef: BitbucketRef,
    val toRef: BitbucketRef,
    val state: BitbucketPullRequestState,
    val createdDate: Date,
    val updatedDate: Date
) : BaseBitbucketEntity<Long>(id) {
    class BitbucketPullRequestUser(val user: BitbucketUser)
}
