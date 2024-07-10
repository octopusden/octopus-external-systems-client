package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TeamcityStep(
    val id: String,
    val name: String,
    val type: String,
    val disabled: Boolean? = null,
    val properties: TeamcityProperties,
)
