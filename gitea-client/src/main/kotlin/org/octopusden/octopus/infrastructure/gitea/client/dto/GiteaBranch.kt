package org.octopusden.octopus.infrastructure.gitea.client.dto

data class GiteaBranch(val name: String, val commit: PayloadCommit): BaseGiteaEntity() {
    data class PayloadCommit(val id: String)
}
