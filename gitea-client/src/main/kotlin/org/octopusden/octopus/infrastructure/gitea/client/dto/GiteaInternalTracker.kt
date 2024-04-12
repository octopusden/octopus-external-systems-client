package org.octopusden.octopus.infrastructure.gitea.client.dto


import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaInternalTracker(
    val allowOnlyContributorsToTrackTime: Boolean? = null,  /* Let only contributors track time (Built-in issue tracker) */
    val enableIssueDependencies: Boolean? = null,  /* Enable dependencies for issues and pull requests (Built-in issue tracker) */
    val enableTimeTracker: Boolean? = null  /* Enable time tracking (Built-in issue tracker) */
) : BaseGiteaEntity()
