package org.octopusden.octopus.infrastructure.jira.dto

import com.fasterxml.jackson.annotation.JsonValue

enum class IssueTypeName(@JsonValue val jiraTypeName: String) {
    TASK("Task")
}
