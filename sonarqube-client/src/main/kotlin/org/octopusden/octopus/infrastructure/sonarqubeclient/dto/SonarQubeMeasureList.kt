package org.octopusden.octopus.infrastructure.sonarqubeclient.dto

class SonarQubeMeasureList(
    paging: SonarQubePaging,
    measures: List<SonarQubeMeasure>
) : SonarQubeEntityList<SonarQubeMeasure>(paging) {
    override val entities: List<SonarQubeMeasure> = measures
}
