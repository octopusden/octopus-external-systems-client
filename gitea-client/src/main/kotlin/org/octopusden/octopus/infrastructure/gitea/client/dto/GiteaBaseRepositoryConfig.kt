package org.octopusden.octopus.infrastructure.gitea.client.dto

open class GiteaBaseRepositoryConfig : BaseGiteaEntity(){
    data class ExternalTracker(
        val externalTrackerFormat: String?, // External Issue Tracker URL Format. Use the placeholders {user}, {repo} and {index} for the username, repository name and issue index.
        val externalTrackerRegexpPattern: String?,  // External Issue Tracker issue regular expression
        val externalTrackerStyle: String?,  // External Issue Tracker Number Format, either numeric, alphanumeric, or regexp
        val externalTrackerUrl: String? // URL of external issue tracker
    )

    data class ExternalWiki(
        val externalWikiUrl: String? // URL of external wiki
    )

    data class InternalTracker(
        val allowOnlyContributorsToTrackTime: Boolean?, // Let only contributors track time (Built-in issue tracker)
        val enableIssueDependencies: Boolean?,  // Enable dependencies for issues and pull requests (Built-in issue tracker)
        val enableTimeTracker: Boolean? // Enable time tracking (Built-in issue tracker)
    )
}
