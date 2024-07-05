package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

//@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TeamcityVcsRootEntry(
    val id: String,
    @JsonProperty("vcs-root")
    val vcsRoot: TeamcityVcsRoot,
    @JsonProperty("checkout-rules")
    val checkoutRules: String
)
