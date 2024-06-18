package org.octopusden.octopus.infrastructure.bitbucket.client.dto

class BitbucketCreatePullRequest(
    val title: String,
    val description: String,
    val fromRef: BitbucketCreatePrRef,
    val toRef: BitbucketCreatePrRef,
    val reviewers: List<BitbucketCreatePullRequestReviewer>,
) {
    val state: BitbucketPullRequestState = BitbucketPullRequestState.OPEN
    val open: Boolean = true
    val closed: Boolean = false
    val locked: Boolean = false
}
