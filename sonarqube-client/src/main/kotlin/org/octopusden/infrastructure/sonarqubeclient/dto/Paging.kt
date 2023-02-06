package org.octopusden.infrastructure.sonarqubeclient.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Paging @JsonCreator constructor(
    @JsonProperty("pageIndex") val pageIndex: Int,
    @JsonProperty("pageSize") val pageSize: Int,
    @JsonProperty("total") val total: Int
)
