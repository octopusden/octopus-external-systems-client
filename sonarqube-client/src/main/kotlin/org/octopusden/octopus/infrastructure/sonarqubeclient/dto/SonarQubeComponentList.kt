package org.octopusden.octopus.infrastructure.sonarqubeclient.dto

class SonarQubeComponentList(
    paging: SonarQubePaging,
    components: List<SonarQubeComponent>
) : SonarQubeEntityList<SonarQubeComponent>(paging) {
    override val entities: List<SonarQubeComponent> = components
}