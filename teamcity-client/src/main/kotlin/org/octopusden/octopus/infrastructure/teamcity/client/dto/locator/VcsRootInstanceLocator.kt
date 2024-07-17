package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

data class VcsRootInstanceLocator(
    val count: Int? = null,
    val buildType: BuildTypeLocator? = null,
    val property: List<PropertyLocator>? = null
) : BaseLocator(mapOf("property" to "property")) {
    override fun toString() = super.toString()
}
