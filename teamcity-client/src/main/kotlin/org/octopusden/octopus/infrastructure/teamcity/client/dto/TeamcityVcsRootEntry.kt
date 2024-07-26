package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityVcsRootEntry(
    val id: String,
    @JsonProperty("vcs-root")
    val vcsRoot: TeamcityVcsRoot,
    @JsonProperty("checkout-rules")
    val checkoutRules: String = "",
)
