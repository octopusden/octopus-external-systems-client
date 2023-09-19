package org.octopusden.octopus.infrastructure.gitea.client.dto

import java.util.Date

data class GiteaRepoCommit(val message: String, val author: CommitUser) {
    data class CommitUser(val name: String, val date: Date)
}
