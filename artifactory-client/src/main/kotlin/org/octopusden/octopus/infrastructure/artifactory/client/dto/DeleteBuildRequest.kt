package org.octopusden.octopus.infrastructure.artifactory.client.dto

data class DeleteBuildRequest(val buildName: String, val buildNumbers: List<String>)