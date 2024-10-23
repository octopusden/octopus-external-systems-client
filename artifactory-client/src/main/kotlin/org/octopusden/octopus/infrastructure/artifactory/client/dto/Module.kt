package org.octopusden.octopus.infrastructure.artifactory.client.dto

@Suppress("unused")
class Module(
    val id: String,
    val type: String?,
    val artifacts: Collection<Artifact>
)