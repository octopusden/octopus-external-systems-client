package org.octopusden.octopus.infrastructure.confluence.client.dto

data class ConfluencePageBody(
    val storage: ConfluenceStorage,
)

data class ConfluenceStorage(
    val value: String,
    val representation: String = "storage",
)
