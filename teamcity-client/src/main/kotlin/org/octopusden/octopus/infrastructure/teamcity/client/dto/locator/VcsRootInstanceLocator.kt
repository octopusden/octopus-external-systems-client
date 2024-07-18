package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

class VcsRootInstanceLocator(
    val count: Int? = null,
    val buildType: BuildTypeLocator? = null,
    val property: List<PropertyLocator>? = null
) : BaseLocator()
