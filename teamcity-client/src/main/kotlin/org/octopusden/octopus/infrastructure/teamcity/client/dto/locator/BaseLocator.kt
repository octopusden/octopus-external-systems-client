package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

open class BaseLocator {

    open fun propertyToString(property: KProperty1<BaseLocator, *>): String {
        val name = property.name
        val value = property.get(this)
        return if (value is BaseLocator) {
            "$name:($value)"
        } else {
            "$name:$value"
        }
    }

    fun locatorListToString(entries: List<Any>, entryName: String) =
        entries.joinToString(",") { "$entryName:(${it})" }

    override fun toString() = this.javaClass.kotlin.memberProperties
        .filter { property -> property.get(this) != null }
        .joinToString(",") { property -> propertyToString(property) }
}
