package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaEditRepository(
    val allowManualMerge: Boolean? = null,
    val allowMergeCommits: Boolean? = null,
    val allowRebase: Boolean? = null,
    val allowRebaseExplicit: Boolean? = null,
    val allowRebaseUpdate: Boolean? = null,
    val allowSquashMerge: Boolean? = null,
    val archived: Boolean? = null,
    val autodetectManualMerge: Boolean? = null,
    val defaultAllowMaintainerEdit: Boolean? = null,
    val defaultBranch: String? = null,
    val defaultDeleteBranchAfterMerge: Boolean? = null,
    val defaultMergeStyle: String? = null,
    val description: String? = null,
    val enablePrune: Boolean? = null,
    val externalTracker: ExternalTracker? = null,
    val externalWiki: ExternalWiki? = null,
    val hasIssues: Boolean? = null,
    val hasProjects: Boolean? = null,
    val hasPullRequests: Boolean? = null,
    val hasWiki: Boolean? = null,
    val ignoreWhitespaceConflicts: Boolean? = null,
    val internalTracker: InternalTracker? = null,
    val mirrorInterval: String? = null,
    val name: String? = null,
    val private: Boolean? = null,
    val template: Boolean? = null,
    val website: String? = null
) {
    data class ExternalTracker(
        val externalTrackerFormat: String?,
        val externalTrackerRegexpPattern: String?,
        val externalTrackerStyle: String?,
        val externalTrackerUrl: String?
    )

    data class ExternalWiki(val externalWikiUrl: String)
    data class InternalTracker(
        val allowOnlyContributorsToTrackTime: Boolean?,
        val enableIssueDependencies: Boolean?,
        val enableTimeTracker: Boolean?
    )
}
