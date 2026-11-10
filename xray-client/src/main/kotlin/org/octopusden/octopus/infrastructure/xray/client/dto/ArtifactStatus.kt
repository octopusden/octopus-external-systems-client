package org.octopusden.octopus.infrastructure.xray.client.dto

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/** [path] is the artifact's path inside [repo]. */
data class ArtifactStatusRequest(
    val repo: String,
    val path: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArtifactStatusResponse(
    val overall: ScanStatus? = null,
    val details: ArtifactStatusDetails? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ArtifactStatusDetails(
    val sca: ScanStatus? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScanStatus(
    val status: ScanState = ScanState.UNKNOWN,
    val time: String? = null,
)

enum class ScanState {
    DONE,
    PENDING,
    SCANNING,
    NOT_SUPPORTED,
    NOT_SCANNED,
    FAILED,

    /** A status this client does not know yet. */
    @JsonEnumDefaultValue
    UNKNOWN,
}
