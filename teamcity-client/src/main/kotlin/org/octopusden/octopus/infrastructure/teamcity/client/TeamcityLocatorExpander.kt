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
            is PropertyLocator.MatchType -> "$name:${escape(value.value)}"
            is TeamcityVCSType -> "$name:${escape(value.value)}"
            is List<*> -> value.joinToString(",") { "$name:(${expand(it)})" }
            is BaseLocator -> "$name:(${expand(value)})"
            else -> "$name:${escape(value.toString())}"
        }

    /**
     * Percent-encode characters that would break the URL when the expanded locator is sent
     * with `@Param(encoded = true)`. Encoding is intentionally narrow:
     *
     * - `%` → `%25` (must be first to avoid escaping our own escapes).
     * - `&` `?` `#` `+` ` ` → percent-encoded — these have query-string semantics; left raw they
     *   would split or corrupt the URL.
     *
     * NOT encoded: `:` `,` `(` `)` — TC locator structural delimiters. Callers whose values
     * legitimately contain those characters must wrap them in TC's `${'$'}any(...)` syntax themselves;
     * automatic escaping would mask such cases as silent value mismatches on the server.
     */
    private fun escape(s: String): String {
        val sb = StringBuilder(s.length)
        for (c in s) {
            when (c) {
                '%' -> sb.append("%25")
                '&' -> sb.append("%26")
                '?' -> sb.append("%3F")
                '#' -> sb.append("%23")
                '+' -> sb.append("%2B")
                ' ' -> sb.append("%20")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

}