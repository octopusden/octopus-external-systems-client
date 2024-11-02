package org.octopusden.octopus.infrastructure.artifactory.client.dto

data class ArtifactoryErrorsResponse(val errors: List<ArtifactoryError>) {
    data class ArtifactoryError(val status: Int, val message: String)

    override fun toString() = errors.joinToString { (status, message) -> "$message ($status)" }
}