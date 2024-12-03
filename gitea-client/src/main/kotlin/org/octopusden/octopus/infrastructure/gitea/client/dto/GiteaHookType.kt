package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

enum class GiteaHookType(@get:JsonValue val jsonValue: String) {
    GITEA("gitea");

    companion object {
        @JvmStatic
        @JsonCreator
        fun forValue(@JsonProperty type: String): GiteaHookType =
            values().find { it.jsonValue == type }
                ?: throw IllegalStateException("type must be in: '${values().joinToString { it.jsonValue }}' but it is $type")
    }
}