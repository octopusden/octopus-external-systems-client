package org.octopusden.octopus.infrastructure.sonarqubeclient

import feign.QueryMap
import feign.RequestLine
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
import org.octopusden.octopus.infrastructure.sonarqubeclient.dto.SonarQubeComponentList
import org.octopusden.octopus.infrastructure.sonarqubeclient.dto.SonarQubeEntityList
import org.octopusden.octopus.infrastructure.sonarqubeclient.dto.SonarQubeMeasure
import org.octopusden.octopus.infrastructure.sonarqubeclient.dto.SonarQubeMeasureList
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val _log: Logger = LoggerFactory.getLogger(SonarQubeClient::class.java)

interface SonarQubeClient {
    @RequestLine("GET api/components/search_projects")
    fun getProjects(@QueryMap requestParams: Map<String, Any>): SonarQubeComponentList

    @RequestLine("GET api/measures/search_history")
    fun getMetricsHistory(@QueryMap requestParams: Map<String, Any>): SonarQubeMeasureList
}

fun SonarQubeClient.getProjects() = execute { parameters -> getProjects(parameters) }

fun SonarQubeClient.getMetricsHistory(
    component: String,
    metrics: List<String>,
    from: LocalDate
) = execute { parameters ->
    getMetricsHistory(
        parameters + mapOf(
            "component" to component,
            "metrics" to metrics.joinToString(","),
            "from" to ISO_LOCAL_DATE.format(from)
        )
    )
}.groupBy { it.metric }.map {
    it.value.reduce { accumulator, measure ->
        SonarQubeMeasure(accumulator.metric, accumulator.history + measure.history)
    }
}

private fun <T> execute(function: (Map<String, Any>) -> SonarQubeEntityList<T>): MutableList<T> {
    var page = 0
    var size = 0
    var total: Int
    val entities = mutableListOf<T>()
    val parameters = mutableMapOf<String, Any>()
    do {
        parameters["p"] = ++page
        val sonarQubeResponse = function.invoke(parameters)
        entities += sonarQubeResponse.entities
        size += sonarQubeResponse.paging.pageSize
        total = sonarQubeResponse.paging.total
    } while (size < total)
    _log.debug("Pages retrieved: $page")
    return entities
}
