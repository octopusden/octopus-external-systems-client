package org.octopusden.octopus.infrastructure.artifactory.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@Suppress("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
data class Dependency(
    val id: String,
    val type: String?,
    val sha1: String?,
    val sha256: String?,
    val md5: String?,
    val scopes: List<String>?,
    val requestedBy: List<List<String>>?
)