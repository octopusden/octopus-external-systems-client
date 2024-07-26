package org.octopusden.octopus.infrastructure.teamcity.client

import feign.Param
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BaseLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.PropertyLocator
import kotlin.reflect.full.memberProperties

class TeamcityLocatorExpander: Param.Expander{
    override fun expand(value: Any?) = when(value){
        is BaseLocator -> value.javaClass.kotlin.memberProperties
                .filter { property -> property.get(value) != null }
                .joinToString(",") { property -> propertyToString(property.name, property.get(value)) }
        else -> value.toString()
    }

    private fun propertyToString(name: String, value: Any?):String =
        when (value) {
            is PropertyLocator.MatchType -> "$name:${value.value}"
            is TeamcityVCSType -> "$name:${value.value}"
            is List<*> -> value.joinToString(",") { "$name:(${expand(it)})" }
            is BaseLocator -> "$name:(${expand(value)})"
            else -> "$name:$value"
        }

}