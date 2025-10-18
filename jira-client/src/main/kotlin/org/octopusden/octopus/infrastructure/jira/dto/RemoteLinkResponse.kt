package org.octopusden.octopus.infrastructure.jira.dto

import java.net.URI

data class RemoteLinkResponse(
    val id: Long,
    val self: URI
)
