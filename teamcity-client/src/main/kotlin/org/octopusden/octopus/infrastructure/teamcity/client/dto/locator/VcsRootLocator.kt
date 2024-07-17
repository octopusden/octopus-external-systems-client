package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

import kotlin.reflect.KProperty1

data class VcsRootLocator(
    val count: Int? = null,
    val project: ProjectLocator? = null,
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
