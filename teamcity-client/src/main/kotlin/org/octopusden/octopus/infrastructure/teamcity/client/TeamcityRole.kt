package org.octopusden.octopus.infrastructure.teamcity.client

import com.fasterxml.jackson.annotation.JsonValue
import feign.Param

enum class TeamcityRole(
    @get:JsonValue val value: String
) {
    SYSTEM_ADMIN("SYSTEM_ADMIN"),
    PROJECT_ADMIN("PROJECT_ADMIN"),
    PROJECT_DEVELOPER("PROJECT_DEVELOPER"),
    PROJECT_VIEWER("PROJECT_VIEWER"),
    AGENT_MANAGER("AGENT_MANAGER");

    class TeamcityRoleExpander : Param.Expander {
        override fun expand(value: Any?) = when (value) {
            is TeamcityRole -> value.value
            else -> throw Exception("Unknown class ${value}")
        }
    }
}
