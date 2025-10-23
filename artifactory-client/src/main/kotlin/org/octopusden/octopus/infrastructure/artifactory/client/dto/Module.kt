package org.octopusden.octopus.infrastructure.artifactory.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
@Suppress("unused")
data class Module(
    val id: String,
    val type: String?,
    val properties: Map<String, String>?,
    val artifacts: Collection<Artifact>?,
    val dependencies: Collection<Dependency>?
)