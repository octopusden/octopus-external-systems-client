package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamcityProjectFeature(
    val id: String? = null,
    val name: String? = null,
    val type: String? = null,
    val disabled: Boolean? = null,
    val inherited: Boolean? = null,
    val href: String? = null,
    val properties: TeamcityProperties? = null,
)
