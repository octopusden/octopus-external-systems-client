package org.octopusden.octopus.infrastructure.sonarqubeclient.dto

abstract class SonarQubeEntityList<T>(
    val paging: SonarQubePaging,
) {
    abstract val entities: List<T>
}
