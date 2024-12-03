package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue

enum class GiteaHookEvent(@get:JsonValue val jsonValue: String) {
    CREATE("create"),
    DELETE("delete"),
    PUSH("push"),
    PULL_REQUEST("pull_request"),
    PULL_REQUEST_LABEL("pull_request_label"),
    PULL_REQUEST_ASSIGN("pull_request_assign"),
    PULL_REQUEST_MILESTONE("pull_request_milestone"),
    PULL_REQUEST_COMMENT("pull_request_comment"),
    PULL_REQUEST_REVIEW_REQUEST("pull_request_review_request"),
    PULL_REQUEST_REVIEW_APPROVED("pull_request_review_approved"),
    PULL_REQUEST_REVIEW_REJECTED("pull_request_review_rejected"),
    PULL_REQUEST_REVIEW_COMMENT("pull_request_review_comment"),
    PULL_REQUEST_SYNC("pull_request_sync"),
    ISSUES("issues"),
    ISSUE_ASSIGN("issue_assign"),
    ISSUE_LABEL("issue_label"),
    ISSUE_MILESTONE("issue_milestone"),
    ISSUE_COMMENT("issue_comment"),
    FORK("fork"),
    REPOSITORY("repository"),
    RELEASE("release"),
    PACKAGE("package"),
    WIKI("wiki");

    companion object {
        @JvmStatic
        @JsonCreator
        fun forValue(@JsonProperty event: String): GiteaHookEvent =
            values().find { it.jsonValue == event }
                ?: throw IllegalStateException("event must be in: '${values().joinToString { it.jsonValue }}' but it is $event")
    }
}