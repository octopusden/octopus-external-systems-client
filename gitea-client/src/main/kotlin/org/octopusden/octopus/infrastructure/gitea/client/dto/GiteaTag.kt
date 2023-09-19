package org.octopusden.octopus.infrastructure.gitea.client.dto

data class GiteaTag(val name: String, val commit: GiteaShortCommit): BaseGiteaEntity()
