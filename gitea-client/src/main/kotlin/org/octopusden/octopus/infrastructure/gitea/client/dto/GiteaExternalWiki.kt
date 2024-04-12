package org.octopusden.octopus.infrastructure.gitea.client.dto


import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaExternalWiki(
    val externalWikiUrl: String? = null  /* URL of external wiki. */
) : BaseGiteaEntity()
