package org.octopusden.octopus.infrastructure.artifactory.client.dto

data class PromoteDockerImageRequest(
    val dockerRepository: String,
    val tag: String,
    val targetRepo: String,
    val targetDockerRepository: String? = null,
    val targetTag: String? = null,
    val copy: Boolean = false
)
