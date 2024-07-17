package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

class VcsRootLocator(
    val count: Int? = null,
    val project: ProjectLocator? = null,
    val property: List<PropertyLocator>? = null
) : BaseLocator(mapOf("property" to "property"))