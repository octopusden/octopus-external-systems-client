package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaGetRepositoryConfig(
    val allowMergeCommits: Boolean? = null,
    val allowRebase: Boolean? = null,
    val allowRebaseExplicit: Boolean? = null,
    val allowRebaseUpdate: Boolean? = null,
    val allowSquashMerge: Boolean? = null,
    val archived: Boolean? = null,
    val archivedAt: String? = null, // ($date-time)
    val avatarUrl: String? = null,
    val cloneUrl: String? = null,
    val createdAt: String? = null, // ($date-time)
    val defaultAllowMaintainerEdit: Boolean? = null,
    val defaultBranch: String? = null,
    val defaultDeleteBranchAfterMerge: Boolean? = null,
    val defaultMergeStyle: String? = null, // set to a merge style to be used by this repository: "merge", "rebase", "rebase-merge", or "squash"
    val description: String? = null,
    val empty: Boolean? = null,
    val externalTracker: ExternalTracker? = null,
    val externalWiki: ExternalWiki? = null,
    val fork: Boolean? = null,
    val forksCount: Long? = null,
    val fullName: String? = null,
    val hasActions: Boolean? = null,
    val hasIssues: Boolean? = null,
    val hasPackages: Boolean? = null,
    val hasProjects: Boolean? = null,
    val hasPullRequests: Boolean? = null,
    val hasReleases: Boolean? = null,
    val hasWiki: Boolean? = null,
    val htmlUrl: String? = null,
    val id: Long,
    val ignoreWhitespaceConflicts: Boolean? = null,
    val internal: Boolean? = null,
    val internalTracker: InternalTracker? = null,
    val language: String? = null,
    val languagesUrl: String? = null,
    val link: String? = null,
    val mirror: Boolean? = null,
    val mirrorInterval: String? = null, // set to a string like 8h30m0s to set the mirror interval time
    val mirrorUpdated: String? = null, // ($date-time)
    val name: String,
    val openIssuesCount: Long? = null,
    val openPrCounter: Long? = null,
    val originalUrl: String? = null,
    val owner: User? = null,
    val parent: Object? = null,
    val permissions: Permission? = null,
    val private: Boolean? = null,
    val releaseCounter: Long? = null,
    val repoTransfer: RepoTransfer? = null,
    val size: Long? = null,
    val sshUrl: String? = null,
    val starsCount: Long? = null,
    val template: Boolean? = null,
    val updatedAt: String? = null,
    val url: String? = null,
    val watchersCount: Long? = null,
    val website: String? = null,
) : GiteaBaseRepositoryConfig() {

    data class User(
        val active: Boolean? = null,    // Is user active
        val avatarUrl: String? = null,  // URL to the user's avatar
        val created: String? = null,
        val description: String? = null, // the user's description
        val email: String? = null,
        val followersCount: Long? = null, // user counts
        val followingCount: Long? = null,
        val fullName: String? = null,  // the user's full name
        val id: Long? = null, // the user's id
        val isAdmin: Boolean? = null, // Is the user an administrator
        val language: String? = null, // User locale
        val lastLogin: String? = null, // ($date-time)
        val location: String? = null, // the user's location
        val login: String? = null, // the user's username
        val loginName: String? = null, // default: empty The user's authentication sign-in name.
        val prohibitLogin: Boolean? = null, // Is user login prohibited
        val restricted: Boolean? = null,    // Is user restricted
        val starredReposCount: Long? = null,
        val visibility: String? = null, // User visibility level option: public, limited, private
        val website: String? = null, //the user's website
    )

    data class Permission(
        val admin: Boolean? = null,
        val pull: Boolean? = null,
        val push: Boolean? = null,
    )

    data class RepoTransfer(
        val doer: User? = null,
        val recipient: User? = null,
        val teams: List<Team>? = null,
        val permission: String? = null,
    ) {
        data class Team(
            // Team represents a team in an organization
            val canCreateOrgRepo: Boolean? = null,
            val description: String? = null,
            val id: Long? = null,
            val includesAllRepositories: Boolean? = null,
            val name: String? = null,
            val organization: Organization? = null,
            val permission: String? = null, //  Enum: [ none, read, write, admin, owner ]
            val units: List<String>? = null, //  example: List [ "repo.code", "repo.issues", "repo.ext_issues", "repo.wiki", "repo.pulls", "repo.releases", "repo.projects", "repo.ext_wiki" ]
            val unitsMap: Map<String, String>? = null, //    example: OrderedMap { "repo.code": "read", "repo.ext_issues": "none", "repo.ext_wiki": "none", "repo.issues": "write", "repo.projects": "none", "repo.pulls": "owner", "repo.releases": "none", "repo.wiki": "admin" }
        ) {
            data class Organization(
                val avatarUrl: String? = null,
                val description: String? = null,
                val email: String? = null,
                val fullName: String? = null,
                val id: Long? = null,
                val location: String? = null,
                val name: String? = null,
                val repoAdminChangeTeamAccess: Boolean? = null,
                val username: String? = null, // deprecated
                val visibility: String? = null,
                val website: String? = null,
            )
        }
    }
}
