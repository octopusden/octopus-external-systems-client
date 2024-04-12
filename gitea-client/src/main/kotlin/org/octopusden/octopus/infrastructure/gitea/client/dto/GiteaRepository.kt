package org.octopusden.octopus.infrastructure.gitea.client.dto

import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaExternalTracker
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaExternalWiki
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaInternalTracker
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaPermission
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaRepoTransfer
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaUser

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaRepository(
    val id: Long,
    val name: String,
    val fullName: String,
    val avatarUrl: String,
    val allowMergeCommits: Boolean? = null,
    val allowRebase: Boolean? = null,
    val allowRebaseExplicit: Boolean? = null,
    val allowRebaseUpdate: Boolean? = null,
    val allowSquashMerge: Boolean? = null,
    val archived: Boolean? = null,
    val archivedAt: String? = null, // java.time.OffsetDateTime
    val cloneUrl: String? = null,
    val createdAt: String? = null, // java.time.OffsetDateTime
    val defaultAllowMaintainerEdit: Boolean? = null,
    val defaultBranch: String? = null,
    val defaultDeleteBranchAfterMerge: Boolean? = null,
    val defaultMergeStyle: String? = null,
    val description: String? = null,
    val empty: Boolean? = null,
    val externalTracker: GiteaExternalTracker? = null,
    val externalWiki: GiteaExternalWiki? = null,
    val fork: Boolean? = null,
    val forksCount: Long? = null,
    val hasActions: Boolean? = null,
    val hasIssues: Boolean? = null,
    val hasPackages: Boolean? = null,
    val hasProjects: Boolean? = null,
    val hasPullRequests: Boolean? = null,
    val hasReleases: Boolean? = null,
    val hasWiki: Boolean? = null,
    val htmlUrl: String? = null,
    val ignoreWhitespaceConflicts: Boolean? = null,
    val `internal`: Boolean? = null,
    val internalTracker: GiteaInternalTracker? = null,
    val language: String? = null,
    val languagesUrl: String? = null,
    val link: String? = null,
    val mirror: Boolean? = null,
    val mirrorInterval: String? = null,
    val mirrorUpdated: String? = null, // java.time.OffsetDateTime
    val openIssuesCount: Long? = null,
    val openPrCounter: Long? = null,
    val originalUrl: String? = null,
    val owner: GiteaUser? = null,
    val parent: GiteaRepository? = null,
    val permissions: GiteaPermission? = null,
    val `private`: Boolean? = null,
    val releaseCounter: Long? = null,
    val repoTransfer: GiteaRepoTransfer? = null,
    val propertySize: Long? = null,
    val sshUrl: String? = null,
    val starsCount: Long? = null,
    val template: Boolean? = null,
    val updatedAt: String? = null, // java.time.OffsetDateTime
    val url: String? = null,
    val watchersCount: Long? = null,
    val website: String? = null
) : BaseGiteaEntity()
