package org.octopusden.octopus.infrastructure.artifactory.client.dto

data class ArtifactoryResponse(val messages: List<ArtifactoryMessage>) {
    data class ArtifactoryMessage(val level: String, val message: String)

    override fun toString() = messages.joinToString { (level, message) -> "$message ($level)" }
}
