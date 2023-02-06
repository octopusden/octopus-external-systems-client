package org.octopusden.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.octopusden.infrastructure.bitbucket.client.exception.NotFoundException

enum class BitbucketExceptionName(
    val exceptionName: String,
    val exceptionSupplier: (String) -> Exception
) {
    NO_SUCH_REPOSITORY(
        "com.atlassian.bitbucket.repository.NoSuchRepositoryException",
        { message -> NotFoundException(message) }
    ),
    NO_SUCH_COMMIT(
        "com.atlassian.bitbucket.repository.NoSuchCommitException",
        { message -> NotFoundException(message) }
    ),
    NO_SUCH_COMMIT_2(
        "com.atlassian.bitbucket.commit.NoSuchCommitException",
        { message -> NotFoundException(message) }
    ),
    NO_SUCH_PROJECT(
        "com.atlassian.bitbucket.project.NoSuchProjectException",
        { message -> NotFoundException(message) }
    ),
    OTHER("", { message -> IllegalStateException(message) });

    companion object {
        @JvmStatic
        @JsonCreator
        fun forValue(@JsonProperty name: String): BitbucketExceptionName =
            values().find { it.exceptionName == name }
                ?: OTHER
    }
}
