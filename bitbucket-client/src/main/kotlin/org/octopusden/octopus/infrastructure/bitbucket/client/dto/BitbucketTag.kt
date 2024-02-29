package org.octopusden.octopus.infrastructure.bitbucket.client.dto

class BitbucketTag(
    id: String,
    displayId: String,
    latestCommit: String
) : BitbucketRef(id, displayId, latestCommit, BitbucketRefType.TAG)
