package org.octopusden.octopus.infrastructure.bitbucket.client.dto

class BitbucketBranch(
    id: String,
    displayId: String,
    latestCommit: String,
    repository: BitbucketRepository? = null
) : BitbucketRef(id, displayId, latestCommit, BitbucketRefType.BRANCH, repository)
