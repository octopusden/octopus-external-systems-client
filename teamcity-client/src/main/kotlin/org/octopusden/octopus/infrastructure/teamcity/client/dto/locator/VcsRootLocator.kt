package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

import kotlin.reflect.KProperty1

//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/vcs-root-instances?locator=property:(name:$fieldName,value:$it,matchType:equals,ignoreCase:true),count:99999,buildType:(id:$buildConfigurationId)"
data class VcsRootInstanceLocator(
    val count: Int? = null,
    val buildType: BuildTypeLocator? = null,
    val property: List<PropertyLocator>? = null
) : BaseLocator() {
    override fun toString() = super.toString()
    override fun propertyToString(property: KProperty1<BaseLocator, *>): String {
        return if (property.name == "property") {
            this.property!!.joinToString(",") { "property:(${it})" }
        } else {
            super.propertyToString(property)
        }
    }
}
