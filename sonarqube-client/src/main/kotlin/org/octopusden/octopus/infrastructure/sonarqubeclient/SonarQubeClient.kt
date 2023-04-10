package org.octopusden.octopus.infrastructure.sonarqubeclient

import org.octopusden.octopus.infrastructure.sonarqubeclient.dto.SonarQubeEntityList
import org.octopusden.octopus.infrastructure.sonarqubeclient.dto.SonarQubeComponent
import feign.QueryMap
import feign.RequestLine

interface SonarQubeClient {

    @RequestLine("GET api/components/search_projects")
    fun getProjects(@QueryMap requestParams: Map<String, Any>): SonarQubeEntityList<SonarQubeComponent>
}


fun SonarQubeClient.getProjects(): List<SonarQubeComponent> {
    val ps = 100
    var p = 1
    var total: Int

    val projects: MutableList<SonarQubeComponent> = mutableListOf()
    do {
        val sonarQubeEntityList = getProjects(mapOf<String, Any>("p" to p, "ps" to ps))

        projects.addAll(sonarQubeEntityList.components)

        total = sonarQubeEntityList.paging.total
        p = sonarQubeEntityList.paging.pageIndex + 1
    } while (projects.size < total)

    return projects
}
