package org.octopusden.octopus.infrastructure.bitbucket.client.dto

data class DefaultReviewersQuery(
    val sourceRepoId: Long,
    val sourceRefId: String,
    val targetRepoId: Long,
    val targetRefId: String
)
