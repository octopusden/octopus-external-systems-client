package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

enum class GiteaHookContentType(@get:JsonValue val jsonValue: String) {
    JSON("json");

    companion object {
        @JvmStatic
        @JsonCreator
        fun forValue(@JsonProperty contentType: String): GiteaHookContentType =
            values().find { it.jsonValue == contentType }
                ?: throw IllegalStateException("content_type must be in: '${values().joinToString { it.jsonValue }}' but it is $contentType")
    }
}