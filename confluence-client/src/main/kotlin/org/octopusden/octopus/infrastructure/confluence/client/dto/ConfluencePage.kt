package org.octopusden.octopus.infrastructure.confluence.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConfluencePage(
    val id: String? = null,
    val type: String = "page",
    val title: String,
    val space: ConfluenceSpace? = null,
    val body: ConfluencePageBody? = null,
    val version: ConfluencePageVersion? = null,
    val ancestors: List<ConfluencePageAncestor>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConfluencePageAncestor(
    val id: String
)