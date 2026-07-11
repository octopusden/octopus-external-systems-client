package org.octopusden.octopus.infrastructure.confluence.client.dto

data class ConfluencePageVersion(
    val number: Int,
    val message: String? = null,
    val minorEdit: Boolean = false,
)
