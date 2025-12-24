package org.octopusden.octopus.infrastructure.bitbucket.client.dto

class BitbucketCreateBuildStatus (
    val state: String,
    val key: String,
    val name: String,
    val url: String
)
