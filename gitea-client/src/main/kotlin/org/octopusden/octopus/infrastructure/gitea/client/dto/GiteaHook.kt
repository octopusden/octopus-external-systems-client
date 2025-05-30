package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaHook(
    val id: Long,
    val type: GiteaHookType,
    val active: Boolean,
    val branchFilter: String,
    val config: Config,
    val events: Collection<GiteaHookEvent>,
) : BaseGiteaEntity() {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class Config(val url: String, val contentType: GiteaHookContentType = GiteaHookContentType.JSON)
}
