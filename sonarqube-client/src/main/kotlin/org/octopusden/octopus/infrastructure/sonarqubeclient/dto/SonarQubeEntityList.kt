package org.octopusden.octopus.infrastructure.sonarqubeclient.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SonarQubeEntityList<T> @JsonCreator constructor(
    @JsonProperty("paging") val paging: Paging, @JsonProperty("components") val components: List<T>
)
