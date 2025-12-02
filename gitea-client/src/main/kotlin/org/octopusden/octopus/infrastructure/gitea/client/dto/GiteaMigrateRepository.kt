package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaMigrateRepository(
    val repoOwner: String,            /* Owner (user or organization) */
    val repoName: String,             /* Name of the new repository */
    val cloneAddr: String,            /* The HTTP(S) or Git 'clone' URL of an existing repository */
    val authUsername: String? = null, /* Username for accessing the source repository */
    val authPassword: String? = null, /* Password for accessing the source repository */
    val authToken: String? = null,    /* Token for accessing the source repository */
)