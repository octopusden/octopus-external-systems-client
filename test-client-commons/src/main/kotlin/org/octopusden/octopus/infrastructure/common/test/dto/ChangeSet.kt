package org.octopusden.octopus.infrastructure.common.test.dto

import java.util.Date

data class ChangeSet(
    val id: String,
    val message: String,
    val repository: String,
    val branch: String,
    val author: String,
    val authorDate: Date
)
