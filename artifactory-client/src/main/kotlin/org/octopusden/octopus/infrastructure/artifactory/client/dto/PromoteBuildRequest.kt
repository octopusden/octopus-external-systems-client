package org.octopusden.octopus.infrastructure.artifactory.client.dto

import java.util.Date

@Suppress("unused")
class PromoteBuildRequest(
    val user: String,
    val targetRepo: String,
    val status: String,
    val timestamp: Date = Date(),
    val dryRun: Boolean = false,
    val copy: Boolean = false,
    val artifacts: Boolean = true,
    val dependencies: Boolean = false,
    val failFast: Boolean = true
)