package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

class BuildTypeLocator(
    val id: String? = null,
    val template: TemplateLocator? = null,
    val count: Int? = null,
    val start: Int? = null,
) : BaseLocator()
