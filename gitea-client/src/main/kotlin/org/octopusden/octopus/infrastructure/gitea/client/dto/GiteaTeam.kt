package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaTeam(
    val id: Long,
    val name: String,
    val canCreateOrgRepo: Boolean? = null,
    val description: String? = null,
    val includesAllRepositories: Boolean? = null,
    val organization: GiteaOrganization? = null,
    val permission: GiteaTeam.Permission? = null,
    val units: List<String>? = null,
    val unitsMap: Map<String, String>? = null
) : BaseGiteaEntity() {
    enum class Permission(val value: String) {
        NONE("none"), READ("read"), WRITE("write"), ADMIN("admin"), OWNER("owner")
    }
}
