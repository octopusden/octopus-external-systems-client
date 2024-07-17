package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

class ProjectLocator(
    val archived: Boolean? = null,
    val count: Int? = null,
    val id: String? = null,
    val parameters: List<PropertyLocator>? = null
) : BaseLocator(mapOf("parameters" to "parameter"))
