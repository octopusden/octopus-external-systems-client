package org.octopusden.octopus.infrastructure.artifactory.client.dto

@Suppress("unused")
data class DeleteBuildRequest(val buildName: String, val buildNumbers: List<String>)