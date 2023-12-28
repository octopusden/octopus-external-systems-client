package org.octopusden.octopus.infrastructure.sonarqubeclient.dto

data class SonarQubeMeasure(val metric: String, val history: List<SonarQubeMetricValue>)
