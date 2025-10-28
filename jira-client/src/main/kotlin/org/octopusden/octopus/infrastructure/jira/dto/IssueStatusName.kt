package org.octopusden.octopus.infrastructure.jira.dto

import com.fasterxml.jackson.annotation.JsonValue

enum class IssueStatusName(@JsonValue val issueStatusName: String) {
    OPEN("Open"),
    IN_PROGRESS("In Progress"),
    RESOLVED("Resolved"),
    CLOSED("Closed"),
    REOPENED("Reopened")
}
