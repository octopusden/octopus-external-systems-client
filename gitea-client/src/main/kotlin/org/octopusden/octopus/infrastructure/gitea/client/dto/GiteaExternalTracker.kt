package org.octopusden.octopus.infrastructure.gitea.client.dto


import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaExternalTracker(
    val externalTrackerFormat: String? = null,  /* External Issue Tracker URL Format. Use the placeholders {user}, {repo} and {index} for the username, repository name and issue index. */
    val externalTrackerRegexpPattern: String? = null,  /* External Issue Tracker issue regular expression */
    val externalTrackerStyle: String? = null,  /* External Issue Tracker Number Format, either `numeric`, `alphanumeric`, or `regexp` */
    val externalTrackerUrl: String? = null  /* URL of external issue tracker. */
) : BaseGiteaEntity()
