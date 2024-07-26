package org.octopusden.octopus.infrastructure.teamcity.client.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TeamcityCreateVcsRootEntry(
    val id: String,
    @JsonProperty("vcs-root")
    val vcsRoot: TeamcityLinkVcsRoot,
    @JsonProperty("checkout-rules")
    val checkoutRules: String = "",
)
