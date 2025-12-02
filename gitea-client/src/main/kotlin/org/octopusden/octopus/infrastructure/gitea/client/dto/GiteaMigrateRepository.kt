package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class GiteaMigrateRepository(
    val repoName: String,                   /* Name of the new repository */
    val cloneAddr: String,                  /* The HTTP(S) or Git 'clone' URL of an existing repository */
    val authPassword: String? = null,       /* Password or token for accessing the source repository */
    val authToken: String? = null,          /* Alternative token (e.g. GitHub token) */
    val authUsername: String? = null,       /* Username for accessing the source repository */
    val awsAccessKeyId: String? = null,     /* Access key for migrations from AWS */
    val awsSecretAccessKey: String? = null, /* Secret key for migrations from AWS */
    val description: String? = null,        /* Description of the new repository */
    val issues: Boolean = true,             /* Import issues */
    val labels: Boolean = true,             /* Import labels */
    val lfs: Boolean = true,                /* Import Git LFS */
    val lfsEndpoint: String? = null,        /* LFS source endpoint */
    val milestones: Boolean = true,         /* Import milestones */
    val mirror: Boolean = false,            /* Create a mirror instead of a regular copy */
    val mirrorInterval: String? = null,     /* Synchronization interval (if mirror = true) */
    val private: Boolean = false,           /* Make the repository private */
    val pullRequests: Boolean = true,       /* Import pull requests */
    val releases: Boolean = true,           /* Import releases */
    val repoOwner: String? = null,          /* Owner (user or organization) */
    val uid: Long? = null,                  /* ID of the owner */
    val service: String = "git",            /* Type of source service (git/github/gitlab/gitea/etc.) */
    val wiki: Boolean = true                /* Import wiki */
)