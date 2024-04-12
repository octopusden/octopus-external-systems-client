package org.octopusden.octopus.infrastructure.gitea.client.dto


import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaPermission(
    val admin: Boolean? = null,
    val pull: Boolean? = null,
    val push: Boolean? = null
) : BaseGiteaEntity()
