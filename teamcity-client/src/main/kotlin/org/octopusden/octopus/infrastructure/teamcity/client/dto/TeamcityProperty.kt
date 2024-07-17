package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamcityProperty(
    val name: String,
    val value: String,
    val inherited: Boolean? = null,
    val type: Type? = null,
){
    data class Type(val rawValue: String)
}


