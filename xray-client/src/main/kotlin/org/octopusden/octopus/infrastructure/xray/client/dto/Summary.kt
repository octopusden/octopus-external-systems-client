package org.octopusden.octopus.infrastructure.xray.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/** Each path is `default/{repo}/{path}/{name}`. */
data class ArtifactSummaryRequest(
    val paths: List<String>,
)

data class ComponentSummaryRequest(
    @get:JsonProperty("component_details")
    val componentDetails: List<ComponentDetail>,
)

data class ComponentDetail(
    @get:JsonProperty("component_id")
    val componentId: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SummaryResponse(
    val artifacts: List<SummaryArtifact> = emptyList(),
    val errors: List<SummaryError> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SummaryArtifact(
    val general: SummaryGeneral? = null,
    val licenses: List<SummaryLicense> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SummaryGeneral(
    val name: String? = null,
    @field:JsonProperty("component_id")
    val componentId: String? = null,
    @field:JsonProperty("pkg_type")
    val pkgType: String? = null,
    val path: String? = null,
    val sha256: String? = null,
)

/** One license and the components Xray found under it, by Xray id (`gav://…`, `npm://…`, `generic://sha256:…`). */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SummaryLicense(
    val name: String,
    @field:JsonProperty("full_name")
    val fullName: String? = null,
    val components: List<String> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SummaryError(
    val identifier: String? = null,
    val error: String? = null,
)
