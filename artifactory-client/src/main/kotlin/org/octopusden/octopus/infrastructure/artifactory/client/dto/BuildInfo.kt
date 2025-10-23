package org.octopusden.octopus.infrastructure.artifactory.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
@Suppress("unused")
data class BuildInfo(
    val name: String,
    val number: String,
    val version: String?,
    val agent: Agent?,
    val buildAgent: BuildAgent?,
    val started: String?,
    val artifactoryPrincipal: String?,
    val modules: Collection<Module>?,
    val statuses: Collection<Status>?
) {
    override fun toString(): String {
        return "$name:$number"
    }
}
