package org.octopusden.octopus.infrastructure.bitbucket.client.dto

data class BitbucketCreateTag(
    val name: String,
    val startPoint: String,
    val message: String
)
