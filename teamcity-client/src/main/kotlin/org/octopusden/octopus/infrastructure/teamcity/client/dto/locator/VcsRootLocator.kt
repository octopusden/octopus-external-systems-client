package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

class VcsRootLocator(
    val id: String? = null,
    val count: Int? = null,
    val project: ProjectLocator? = null,
    val property: List<PropertyLocator>? = null
) : BaseLocator()