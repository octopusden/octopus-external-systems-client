package org.octopusden.octopus.infrastructure.gitea.client.dto

import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaTeam
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaUser

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaRepoTransfer(
    val doer: GiteaUser? = null,
    val recipient: GiteaUser? = null,
    val teams: List<GiteaTeam>? = null
) : BaseGiteaEntity()
