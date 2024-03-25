package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaOrganization(
    val id: Long,
    val name: String,
    val fullName: String,
    val avatarUrl: String
) : BaseGiteaEntity()
