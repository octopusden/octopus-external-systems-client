package org.octopusden.octopus.infrastructure.gitea.client.dto

import java.util.Date

data class GiteaCommit(
    val sha: String,
    val created: Date,
    val commit: GiteaRepoCommit,
    val parents: Collection<GiteaShortCommit>,
    val author: GiteaUser? = null
) : BaseGiteaEntity() {
    data class GiteaRepoCommit(val message: String, val author: GiteaRepoCommitUser)
    data class GiteaRepoCommitUser(val name: String)
}
