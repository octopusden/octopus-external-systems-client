package org.octopusden.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

enum class BitbucketLinkName(val propertyName: String) {
    HTTP("http"),
    SSH("ssh");

    companion object {
        @JvmStatic
        @JsonCreator
        fun forValue(@JsonProperty name: String): BitbucketLinkName =
            values().find { it.propertyName == name }
                ?: throw IllegalStateException("Repository Link Name must be in: '${values().joinToString()}}' but got $name")
    }
}
