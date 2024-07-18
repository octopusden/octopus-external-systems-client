package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

import org.octopusden.octopus.infrastructure.teamcity.client.TeamcityLocatorExpander
import kotlin.reflect.full.memberProperties

open class BaseLocator{

    override fun toString() = TeamcityLocatorExpander().expand(this)
/*
    override fun toString() = TeamcityLocatorExpander(this) this.javaClass.kotlin.memberProperties
        .filter { property -> property.get(this) != null }
        .joinToString(",") { property -> propertyToString(property.name, property.get(this)) }

    private fun propertyToString(name: String, value: Any?):String =
        when (value) {
            is List<*> -> value.joinToString(",") { "$name:(${it})" }
            is BaseLocator -> "$name:($value)"
            else -> "$name:$value"
        }
*/
}
