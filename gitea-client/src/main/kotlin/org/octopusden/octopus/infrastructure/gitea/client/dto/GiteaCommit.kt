package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.annotation.JsonValue
import java.util.Date

data class GiteaCommit(
    val sha: String,
    val created: Date,
    val commit: GiteaRepoCommit,
    val parents: Collection<GiteaShortCommit>,
    val author: GiteaUser?,
    val files: Collection<GiteaCommitAffectedFile>?
) : BaseGiteaEntity() {
    data class GiteaRepoCommit(val message: String, val author: GiteaRepoCommitUser)
    data class GiteaRepoCommitUser(val name: String)
    data class GiteaCommitAffectedFile(val filename: String, val status: GiteaCommitAffectedFileStatus)
    enum class GiteaCommitAffectedFileStatus(
        @get:JsonValue
        val jsonValue: String
    ) {
        ADDED("added"), MODIFIED("modified"), REMOVED("removed")
    }
}
