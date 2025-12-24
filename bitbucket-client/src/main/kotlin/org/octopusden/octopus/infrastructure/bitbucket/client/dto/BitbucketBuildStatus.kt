package org.octopusden.octopus.infrastructure.bitbucket.client.dto

class BitbucketBuildStatus(
    val state: String,
    val key: String,
    val name: String,
    val url: String,
    val dateAdded: Long
)
