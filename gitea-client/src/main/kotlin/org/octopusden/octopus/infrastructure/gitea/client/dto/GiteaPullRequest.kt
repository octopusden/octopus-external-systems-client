package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.Date

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaPullRequest(
    val id: Long,
    val number: Long,
    val title: String,
    val body: String,
    val user: GiteaUser,
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    val assignees: List<GiteaUser>,
    @JsonSetter(nulls = Nulls.AS_EMPTY, contentNulls = Nulls.SKIP)
    //for each team in reviewers `requested_reviewers` collection contains null
    val requestedReviewers: List<GiteaUser>,
    val base: GiteaPullRequestBranch,
    val head: GiteaPullRequestBranch,
    val state: GiteaPullRequestState,
    val merged: Boolean,
    val createdAt: Date,
    val updatedAt: Date
) : BaseGiteaEntity() {
    data class GiteaPullRequestBranch(val label: String)
    enum class GiteaPullRequestState(
        @get:JsonValue
        val jsonValue: String
    ) {
        OPEN("open"), CLOSED("closed")
    }
}