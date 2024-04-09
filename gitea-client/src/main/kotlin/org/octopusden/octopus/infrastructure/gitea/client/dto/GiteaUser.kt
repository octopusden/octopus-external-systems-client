package org.octopusden.octopus.infrastructure.gitea.client.dto


import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaUser(
    val id: Long,
    val username: String,
    val avatarUrl: String,
    val active: Boolean? = null,  /* Is user active */
    val created: String? = null, // java.time.OffsetDateTime?
    val description: String? = null,  /* the user's description */
    val email: String? = null,
    val followersCount: Long? = null,  /* user counts */
    val followingCount: Long? = null,
    val fullName: String? = null,  /* the user's full name */
    val isAdmin: Boolean? = null,  /* Is the user an administrator */
    val language: String? = null,  /* User locale */
    val lastLogin: String? = null, // java.time.OffsetDateTime?
    val location: String? = null,  /* the user's location */
    val login: String? = null,  /* the user's username */
    val loginName: String? = "empty",  /* the user's authentication sign-in name. */
    val prohibitLogin: Boolean? = null,  /* Is user login prohibited */
    val restricted: Boolean? = null,  /* Is user restricted */
    val starredReposCount: Long? = null,
    val visibility: String? = null,  /* User visibility level option: public, limited, private */
    val website: String? = null  /* the user's website */
) : BaseGiteaEntity()
