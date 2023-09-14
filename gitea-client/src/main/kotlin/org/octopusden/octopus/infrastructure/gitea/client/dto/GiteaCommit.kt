package org.octopusden.octopus.infrastructure.gitea.client.dto

data class GiteaCommit(val sha: String, val commit: GiteaCommitMeta, val parents: Collection<GiteaShortCommit>)
