package org.octopusden.octopus.infrastructure.sonarqubeclient.dto

import java.time.OffsetDateTime

class SonarQubeMetricValue(val date: OffsetDateTime, val value: String = "0")