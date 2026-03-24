package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.octopusden.octopus.infrastructure.teamcity.client.TeamcityRoleScopeType

@JsonDeserialize(using = TeamcityRoleScopeDeserializer::class)
data class TeamcityRoleScope(val type: TeamcityRoleScopeType, val project: String? = null)

class TeamcityRoleScopeDeserializer : JsonDeserializer<TeamcityRoleScope>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TeamcityRoleScope {
        val parts = p.text.split(":", limit = 2)
        val type = TeamcityRoleScopeType.fromString(parts[0])
        val project = parts.getOrNull(1)?.takeIf { it.isNotEmpty() }
        return TeamcityRoleScope(type, project)
    }
}
