package org.octopusden.octopus.infrastructure.jira.dto

import com.fasterxml.jackson.annotation.JsonValue

enum class IssueTypeName(@JsonValue val jiraTypeName: String) {
    TASK("Task"),
    NEW_FEATURE("New Feature"), 
    ENHANCEMENT("Enhancement"),
    BUG("Bug"),
    EPIC("Epic"),
    RESEARCH("Research"),
    DOCUMENTATION("Documentation"),
    DEPLOYMENT("Deployment")
}
