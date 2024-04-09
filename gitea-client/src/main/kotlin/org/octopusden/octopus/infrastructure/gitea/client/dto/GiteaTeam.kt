package org.octopusden.octopus.infrastructure.gitea.client.dto

import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaOrganization

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaTeam(
    val canCreateOrgRepo: Boolean? = null,
    val description: String? = null,
    val id: Long? = null,
    val includesAllRepositories: Boolean? = null,
    val name: String? = null,
    val organization: GiteaOrganization? = null,
    val permission: GiteaTeam.Permission? = null,
    val units: List<String>? = null,
    val unitsMap: Map<String, String>? = null
) : BaseGiteaEntity() {
    enum class Permission(val value: String) {
    }
}
