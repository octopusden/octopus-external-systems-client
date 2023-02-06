package org.octopusden.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

class BitbucketCreateRepository(
    val name: String,
    val forkable: Boolean = false,
    val scmId: ScmType = ScmType.GIT,
    val defaultBranch: String = "master"
) {
    enum class ScmType(val scmId: String) {
        GIT("git");

        @JsonValue
        fun value() = scmId

        companion object {
            @JvmStatic
            @JsonCreator
            fun forValue(@JsonProperty scmId: String): ScmType =
                values().find { it.scmId == scmId }
                    ?: throw IllegalStateException("scmId must be in: '${values().joinToString()}}' but it is $scmId")
        }
    }
}

