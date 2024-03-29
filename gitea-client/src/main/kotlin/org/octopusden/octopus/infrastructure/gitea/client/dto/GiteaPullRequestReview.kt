package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.util.Date

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaPullRequestReview(
    val id: Long,
    val user: GiteaUser,
    val state: GiteaPullRequestReviewState,
    val dismissed: Boolean,
    val submittedAt: Date,
    val updatedAt: Date
) : BaseGiteaEntity() {
    enum class GiteaPullRequestReviewState(
        @get:JsonValue
        val jsonValue: String
    ) {
        APPROVED("APPROVED"),
        PENDING("PENDING"),
        COMMENT("COMMENT"),
        REQUEST_CHANGES("REQUEST_CHANGES"),
        REQUEST_REVIEW("REQUEST_REVIEW"),
        UNKNOWN("")
    }
}
