package org.octopusden.octopus.infrastructure.sonarqubeclient.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SonarQubeComponent @JsonCreator constructor(
    @JsonProperty("key") val key: String,
    @JsonProperty("name") val name: String
)
