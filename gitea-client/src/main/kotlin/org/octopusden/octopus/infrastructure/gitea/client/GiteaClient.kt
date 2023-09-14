package org.octopusden.octopus.infrastructure.gitea.client

import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaBranch
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCommit
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreatePullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaPullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaTag
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaUser
import org.octopusden.octopus.infrastructure.gitea.client.exception.NotFoundException
import java.util.Date


const val ORG_PATH = "api/v1/orgs"
const val REPO_PATH = "api/v1/repos"
const val ENTITY_LIMIT = 100

interface GiteaClient {
    @RequestLine("GET $ORG_PATH")
    fun getOrganizations(@QueryMap requestParams: Map<String, Any>): Collection<GiteaOrganization>

    @RequestLine("GET $REPO_PATH/search")
    fun getRepositories(@QueryMap requestParams: Map<String, Any>): Collection<GiteaRepository>

    @RequestLine("POST $ORG_PATH")
    @Headers("Content-Type: application/json")
    fun createOrganization(dto: GiteaCreateOrganization)

    @RequestLine("GET $ORG_PATH/{organization}")
    @Throws(NotFoundException::class)
    fun getOrganization(@Param("organization") organization: String): GiteaOrganization

    @RequestLine("GET $ORG_PATH/{organization}/repos")
    fun getRepositories(
        @Param("organization") organization: String,
        @QueryMap requestParams: Map<String, Any>
    ): Collection<GiteaRepository>

    @RequestLine("GET $REPO_PATH/{organization}/{repository}")
    @Throws(NotFoundException::class)
    fun getRepository(
        @Param("organization") organization: String,
        @Param("repository") repository: String
    ): GiteaRepository

    @RequestLine("POST $ORG_PATH/{organization}/repos")
    @Headers("Content-Type: application/json")
    fun createRepository(@Param("organization") organization: String, dto: GiteaCreateRepository)

    @RequestLine("DELETE $REPO_PATH/{organization}/{repository}")
    fun deleteRepository(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
    )

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/commits")
    fun getCommits(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @QueryMap requestParams: Map<String, Any>
    ): Collection<GiteaCommit>

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/git/commits/{sha}")
    @Throws(NotFoundException::class)
    fun getCommit(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @Param("sha") sha: String
    ): GiteaCommit

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/tags")
    fun getTags(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @QueryMap requestParams: Map<String, Any>,
    ): Collection<GiteaTag>

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/branches")
    fun getBranches(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @QueryMap requestParams: Map<String, Any>,
    ): Collection<GiteaBranch>

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/reviewers")
    fun getDefaultReviewers(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
    ): Collection<GiteaUser>

    @RequestLine("POST $REPO_PATH/{organization}/{repository}/pulls")
    @Headers("Content-Type: application/json")
    fun createPullRequest(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        dto: GiteaCreatePullRequest
    ): GiteaPullRequest
}

fun GiteaClient.getOrganizations(): List<GiteaOrganization> {
    return execute({ parameters: Map<String, Any> -> getOrganizations(parameters) })
}

fun GiteaClient.getRepositories(): List<GiteaRepository> =
    execute({ parameters: Map<String, Any> -> getRepositories(parameters) })

fun GiteaClient.getRepositories(projectKey: String): List<GiteaRepository> =
    execute({ parameters: Map<String, Any> -> getRepositories(projectKey, parameters) })

fun GiteaClient.getCommits(
    projectKey: String,
    repository: String,
    sinceDate: Date?,
    until: String?
): List<GiteaCommit> {
    val limitParameters = mutableMapOf<String, Any>()
    until?.let { untilValue ->
        limitParameters["sha"] = untilValue
    }

    val filter = sinceDate?.let { fromDateValue -> { c: GiteaCommit -> c.commit.author.date > fromDateValue } }
        ?: { true }

    return execute(
        { parameters: Map<String, Any> -> getCommits(projectKey, repository, parameters + limitParameters) },
        filter
    )
}

fun GiteaClient.getTags(
    projectKey: String,
    repository: String
): List<GiteaTag> = execute({ parameters: Map<String, Any> -> getTags(projectKey, repository, parameters) })

fun GiteaClient.getBranches(
    projectKey: String,
    repository: String
): List<GiteaBranch> = execute({ parameters: Map<String, Any> -> getBranches(projectKey, repository, parameters) })

fun GiteaClient.createPullRequestWithDefaultReviewers(
    projectKey: String,
    repository: String,
    sourceBranch: String,
    targetBranch: String,
    title: String,
    description: String
): GiteaPullRequest {
    val branches = getBranches(projectKey, repository)
        .map { branch -> branch.name }


    fun checkBranch(type: String, branchName: String) {
        if (!branches.contains(branchName)) {
            throw NotFoundException("$type branch '$branchName' not found in '$projectKey:$repository'")
        }
    }

    checkBranch("Source", sourceBranch)
    checkBranch("Target", targetBranch)

    val defaultReviewers = getDefaultReviewers(projectKey, repository)
        .map { u -> u.username }
        .toMutableSet()

    val assignee = defaultReviewers.firstOrNull()
    assignee?.let { defaultReviewers.remove(assignee) }


    return createPullRequest(
        projectKey,
        repository,
        GiteaCreatePullRequest(title, description, sourceBranch, targetBranch, defaultReviewers, assignee)
    )
}

private fun <T> execute(
    function: (Map<String, Any>) -> Collection<T>,
    filter: (element: T) -> Boolean = { true }
): MutableList<T> {
    var pageStart = 1
    val entities = mutableListOf<T>()
    val staticParameters = mutableMapOf<String, Any>("limit" to ENTITY_LIMIT)
    do {
        staticParameters["page"] = pageStart
        val currentPartEntities = function.invoke(staticParameters)
        val inFilter: Boolean = with(currentPartEntities.all(filter)) {
            entities += if (this) {
                currentPartEntities
            } else {
                currentPartEntities.filter(filter)
            }
            this
        }
        pageStart++
    } while (currentPartEntities.size == ENTITY_LIMIT && inFilter)
    return entities
}
