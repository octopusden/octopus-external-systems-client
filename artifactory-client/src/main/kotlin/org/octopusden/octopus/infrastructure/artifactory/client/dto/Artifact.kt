package org.octopusden.octopus.infrastructure.artifactory.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
@Suppress("unused")
data class Artifact(
    val name: String,
    val type: String?,
    val sha1: String?,
    val sha256: String?,
    val md5: String?,
    /** The file's path in the repository, its name included; build-info records it for docker image layers and manifests. */
    val path: String? = null,
)
