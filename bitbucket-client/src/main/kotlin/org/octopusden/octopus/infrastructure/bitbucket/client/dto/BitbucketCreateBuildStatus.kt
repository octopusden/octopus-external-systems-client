package org.octopusden.octopus.infrastructure.bitbucket.client.dto

data class BitbucketCreateBuildStatus(
    val state: String,
    val key: String,
    val name: String,
    val url: String
)
