package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaEditRepoOption(
    val allowManualMerge: Boolean? = null,  /* either `true` to allow mark pr as merged manually, or `false` to prevent it. */
    val allowMergeCommits: Boolean? = null,  /* either `true` to allow merging pull requests with a merge commit, or `false` to prevent merging pull requests with merge commits. */
    val allowRebase: Boolean? = null,  /* either `true` to allow rebase-merging pull requests, or `false` to prevent rebase-merging. */
    val allowRebaseExplicit: Boolean? = null,  /* either `true` to allow rebase with explicit merge commits (--no-ff), or `false` to prevent rebase with explicit merge commits. */
    val allowRebaseUpdate: Boolean? = null,  /* either `true` to allow updating pull request branch by rebase, or `false` to prevent it. */
    val allowSquashMerge: Boolean? = null,  /* either `true` to allow squash-merging pull requests, or `false` to prevent squash-merging. */
    val archived: Boolean? = null,  /* set to `true` to archive this repository. */
    val autodetectManualMerge: Boolean? = null,  /* either `true` to enable AutodetectManualMerge, or `false` to prevent it. Note: In some special cases, misjudgments can occur. */
    val defaultAllowMaintainerEdit: Boolean? = null,  /* set to `true` to allow edits from maintainers by default */
    val defaultBranch: String? = null,  /* sets the default branch for this repository. */
    val defaultDeleteBranchAfterMerge: Boolean? = null,  /* set to `true` to delete pr branch after merge by default */
    val defaultMergeStyle: String? = null,  /* set to a merge style to be used by this repository: \"merge\", \"rebase\", \"rebase-merge\", or \"squash\". */
    val description: String? = null,  /* a short description of the repository. */
    val enablePrune: Boolean? = null,  /* enable prune - remove obsolete remote-tracking references */
    val externalTracker: GiteaExternalTracker? = null,
    val externalWiki: GiteaExternalWiki? = null,
    val hasActions: Boolean? = null,  /* either `true` to enable actions unit, or `false` to disable them. */
    val hasIssues: Boolean? = null,  /* either `true` to enable issues for this repository or `false` to disable them. */
    val hasPackages: Boolean? = null,  /* either `true` to enable packages unit, or `false` to disable them. */
    val hasProjects: Boolean? = null,  /* either `true` to enable project unit, or `false` to disable them. */
    val hasPullRequests: Boolean? = null,  /* either `true` to allow pull requests, or `false` to prevent pull request. */
    val hasReleases: Boolean? = null,  /* either `true` to enable releases unit, or `false` to disable them. */
    val hasWiki: Boolean? = null,  /* either `true` to enable the wiki for this repository or `false` to disable it. */
    val ignoreWhitespaceConflicts: Boolean? = null,  /* either `true` to ignore whitespace for conflicts, or `false` to not ignore whitespace. */
    val internalTracker: GiteaInternalTracker? = null,
    val mirrorInterval: String? = null,  /* set to a string like `8h30m0s` to set the mirror interval time */
    val name: String? = null,  /* name of the repository */
    val `private`: Boolean? = null,  /* either `true` to make the repository private or `false` to make it public. Note: you will get a 422 error if the organization restricts changing repository visibility to organization owners and a non-owner tries to change the value of private. */
    val template: Boolean? = null,  /* either `true` to make this repository a template or `false` to make it a normal repository */
    val website: String? = null  /* a URL with more information about the repository. */
) : BaseGiteaEntity()
