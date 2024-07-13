package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

data class BuildTypeLocator(
    val id: String? = null,
) : BaseLocator() {
    override fun toString() = super.toString()
}
