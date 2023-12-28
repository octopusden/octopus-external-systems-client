package org.octopusden.octopus.infrastructure.sonarqubeclient.dto

data class SonarQubePaging(
    val pageIndex: Int,
    val pageSize: Int,
    val total: Int
)
