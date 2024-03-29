package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaRepositoryConfig(
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
) : GiteaBaseRepositoryConfig() {
    constructor(config: GiteaGetRepositoryConfig) : this(
        // allowManualMerge = config.allowManualMerge,
        allowMergeCommits = config.allowMergeCommits,
        allowRebase = config.allowRebase,
        allowRebaseExplicit = config.allowRebaseExplicit,
        allowRebaseUpdate = config.allowRebaseUpdate,
        allowSquashMerge = config.allowSquashMerge,
        archived = config.archived,
        // autodetectManualMerge = config.autodetectManualMerge,
        defaultAllowMaintainerEdit = config.defaultAllowMaintainerEdit,
        defaultBranch = config.defaultBranch,
        defaultDeleteBranchAfterMerge = config.defaultDeleteBranchAfterMerge,
        defaultMergeStyle = config.defaultMergeStyle,
        description = config.description,
        // enablePrune = config.enablePrune,
        externalTracker = config.externalTracker,
        externalWiki = config.externalWiki,
        hasIssues = config.hasIssues,
        hasProjects = config.hasProjects,
        hasPullRequests = config.hasPullRequests,
        hasWiki = config.hasWiki,
        ignoreWhitespaceConflicts = config.ignoreWhitespaceConflicts,
        internalTracker = config.internalTracker,
        mirrorInterval = config.mirror?.let { m ->
            if (m) {
                config.mirrorInterval
            } else {
                null
            }
        },
        name = config.name,
        private = config.private,
        template = config.template,
        website = config.website
    )

}
