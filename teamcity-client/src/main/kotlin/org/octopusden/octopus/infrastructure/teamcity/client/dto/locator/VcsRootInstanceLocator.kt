package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

import kotlin.reflect.KProperty1

data class VcsRootInstanceLocator(
    val count: Int? = null,
    val buildType: BuildTypeLocator? = null,
    val property: List<PropertyLocator>? = null
) : BaseLocator() {
    override fun toString() = super.toString()
    override fun propertyToString(property: KProperty1<BaseLocator, *>): String {
        return if (property.name == "property") {
            locatorListToString(this.property!!, "property")
        } else {
            super.propertyToString(property)
        }
    }
}
