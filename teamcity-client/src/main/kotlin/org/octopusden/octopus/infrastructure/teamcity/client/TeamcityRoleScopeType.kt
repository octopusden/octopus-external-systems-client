package org.octopusden.octopus.infrastructure.teamcity.client

enum class TeamcityRoleScopeType(val stringValue: String) {
    GLOBAL("g"), PROJECT("p");

    companion object {
        fun fromString(value: String): TeamcityRoleScopeType =
            entries.firstOrNull { it.stringValue == value }
                ?: throw IllegalArgumentException("Unknown scope type: '$value'")
    }
}
