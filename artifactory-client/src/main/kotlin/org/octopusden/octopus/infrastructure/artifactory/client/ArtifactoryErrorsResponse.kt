package org.octopusden.octopus.infrastructure.artifactory.client

data class ArtifactoryErrorsResponse(val errors: List<ArtifactoryError>) {
    data class ArtifactoryError(val status: Int, val message: String)
}