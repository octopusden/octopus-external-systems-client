package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

class ProjectLocator(
    val archived: Boolean? = null,
    val count: Int? = null,
    val id: String? = null,
    val name: String? = null,
    val parameter: List<PropertyLocator>? = null
) : BaseLocator()
