package org.octopusden.octopus.infrastructure.artifactory.client.dto

@Suppress("unused")
data class ArtifactoryResponse(val messages: List<ArtifactoryMessage>) {
    class ArtifactoryMessage(val level: String, val message: String)
}
