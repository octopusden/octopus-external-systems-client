package org.octopusden.octopus.infrastructure.confluence.client.dto

data class ConfluencePageCreateRequest(
    val type: String = "page",
    val title: String,
    val space: ConfluenceSpace,
    val ancestors: List<ConfluencePageAncestor>,
    val body: ConfluencePageBody
)
