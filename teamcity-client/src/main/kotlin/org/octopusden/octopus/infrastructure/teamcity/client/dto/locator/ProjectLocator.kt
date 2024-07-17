package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

import kotlin.reflect.KProperty1

data class ProjectLocator(
    val archived: Boolean? = null,
    val count: Int? = null,
    val id: String? = null,
    val parameters: List<PropertyLocator>? = null
) : BaseLocator() {
    override fun propertyToString(property: KProperty1<BaseLocator, *>): String {
        return if (property.name == "parameters") {
            locatorListToString(parameters!!, "parameter")
        } else {
            super.propertyToString(property)
        }
    }

    override fun toString() = super.toString()
}
