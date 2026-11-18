package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

@JsonInclude(JsonInclude.Include.NON_NULL)
class BitbucketCreateRepository(
    val name: String,
    val forkable: Boolean = false,
    val scmId: ScmType = ScmType.GIT,
    val defaultBranch: String = "master",
) {
    var description: String? = null
        private set
    var public: Boolean? = null
        private set

    // description is required here so that BitbucketCreateRepository(name) keeps resolving to the primary constructor
    constructor(
        name: String,
        forkable: Boolean = false,
        scmId: ScmType = ScmType.GIT,
        defaultBranch: String = "master",
        description: String?,
        public: Boolean? = null,
    ) : this(name, forkable, scmId, defaultBranch) {
        this.description = description
        this.public = public
    }

    enum class ScmType(
        val scmId: String,
    ) {
        GIT("git"),
        ;

        @JsonValue
        fun value() = scmId

        companion object {
            @JvmStatic
            @JsonCreator
            fun forValue(
                @JsonProperty scmId: String,
            ): ScmType =
                values().find { it.scmId == scmId }
                    ?: throw IllegalStateException("scmId must be in: '${values().joinToString()}}' but it is $scmId")
        }
    }
}
