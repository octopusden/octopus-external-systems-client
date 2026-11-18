package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
class GiteaCreateRepository(
    val name: String,
    val description: String? = null,
    val `private`: Boolean? = null,
    @get:JsonProperty("default_branch")
    @param:JsonProperty("default_branch")
    val defaultBranch: String? = null,
)
