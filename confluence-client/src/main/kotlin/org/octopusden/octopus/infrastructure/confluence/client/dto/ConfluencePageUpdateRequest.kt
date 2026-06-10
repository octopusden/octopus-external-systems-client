package org.octopusden.octopus.infrastructure.confluence.client.dto

data class ConfluencePageUpdateRequest(
    val id: String,
    val type: String = "page",
    val title: String,
    val space: ConfluenceSpace? = null,
    val body: ConfluencePageBody,
    val version: ConfluencePageVersion,
    val ancestors: List<ConfluencePageAncestor>? = null
)