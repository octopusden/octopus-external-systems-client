package org.octopusden.octopus.infrastructure.artifactory.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/** The answer to an AQL `builds.find(...)` query, whose rows are builds, not items. */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AqlBuildSearchResponse(
    val results: List<AqlBuild> = emptyList(),
    val range: AqlRange? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AqlBuild(
    @field:JsonProperty("build.name")
    val name: String? = null,
    @field:JsonProperty("build.number")
    val number: String? = null,
    @field:JsonProperty("build.repo")
    val repo: String? = null,
)
