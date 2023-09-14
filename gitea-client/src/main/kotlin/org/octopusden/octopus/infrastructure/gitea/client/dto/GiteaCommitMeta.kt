package org.octopusden.octopus.infrastructure.gitea.client.dto

import java.util.Date

data class GiteaCommitMeta(val message: String, val author: CommitUser) {
    data class CommitUser(val date: Date)
}
