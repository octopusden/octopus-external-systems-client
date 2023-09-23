package org.octopusden.octopus.infrastructure.gitea.client

import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import org.octopusden.octopus.infrastructure.gitea.client.dto.BaseGiteaEntity
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaBranch
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCommit
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreatePullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaEntityList
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaPullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaTag
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaUser
import org.octopusden.octopus.infrastructure.gitea.client.exception.NotFoundException
import java.util.Date


const val ORG_PATH = "api/v1/orgs"
const val REPO_PATH = "api/v1/repos"
const val ENTITY_LIMIT = 50

interface GiteaClient {
    @RequestLine("GET $ORG_PATH")
    fun getOrganizations(@QueryMap requestParams: Map<String, Any>): GiteaEntityList<GiteaOrganization>

    @RequestLine("GET $REPO_PATH/search")
    fun getRepositories(@QueryMap requestParams: Map<String, Any>): GiteaEntityList<GiteaRepository>

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
    ): GiteaEntityList<GiteaRepository>

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
    ): GiteaEntityList<GiteaCommit>

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/git/commits/{sha}", decodeSlash = false)
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
    ): GiteaEntityList<GiteaTag>

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/branches")
    fun getBranches(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @QueryMap requestParams: Map<String, Any>,
    ): GiteaEntityList<GiteaBranch>?

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

fun GiteaClient.getOrganizations(): Collection<GiteaOrganization> {
    return execute({ parameters: Map<String, Any> -> getOrganizations(parameters) })
}

fun GiteaClient.getRepositories(): Collection<GiteaRepository> =
    execute({ parameters: Map<String, Any> -> getRepositories(parameters) })

fun GiteaClient.getRepositories(organization: String): Collection<GiteaRepository> =
    execute({ parameters: Map<String, Any> -> getRepositories(organization, parameters) })

fun GiteaClient.getCommits(
    organization: String,
    repository: String,
    sinceDate: Date?,
    until: String?
): Collection<GiteaCommit> {
    val limitParameters = mutableMapOf<String, Any>(
        "limit" to ENTITY_LIMIT,
        "stat" to false,
        "verification" to false,
        "files" to false
    )
    until?.let { untilValue ->
        limitParameters["sha"] = untilValue
    }

    val filter = sinceDate?.let { fromDateValue -> { c: GiteaCommit -> c.commit.author.date >= fromDateValue } }
        ?: { true }

    return execute(
        { parameters: Map<String, Any> -> getCommits(organization, repository, parameters + limitParameters) },
        filter
    )
}

fun GiteaClient.getTags(
    organization: String,
    repository: String
): Collection<GiteaTag> = execute({ parameters: Map<String, Any> -> getTags(organization, repository, parameters) })

fun GiteaClient.getBranches(
    organization: String,
    repository: String
): Collection<GiteaBranch> = execute({ parameters: Map<String, Any> ->
    getBranches(organization, repository, parameters) ?: GiteaEntityList(false, emptyList())
})

fun GiteaClient.createPullRequestWithDefaultReviewers(
    organization: String,
    repository: String,
    sourceBranch: String,
    targetBranch: String,
    title: String,
    description: String
): GiteaPullRequest {
    val branches = getBranches(organization, repository)
        .map { branch -> branch.name }

    fun checkBranch(type: String, branchName: String) {
        if (!branches.contains(branchName)) {
            throw NotFoundException("$type branch '$branchName' not found in '$organization:$repository'")
        }
    }

    checkBranch("Source", sourceBranch)
    checkBranch("Target", targetBranch)

    val defaultReviewers = getDefaultReviewers(organization, repository)
        .map { u -> u.username }
        .toMutableSet()

    val assignee = defaultReviewers.firstOrNull()
    assignee?.let { defaultReviewers.remove(assignee) }


    return createPullRequest(
        organization,
        repository,
        GiteaCreatePullRequest(title, description, sourceBranch, targetBranch, defaultReviewers, assignee)
    )
}

private fun <T : BaseGiteaEntity> execute(
    function: (Map<String, Any>) -> GiteaEntityList<T>,
    filter: (element: T) -> Boolean = { true }
): Collection<T> {
    var pageStart = 1
    val entities = mutableListOf<T>()
    val staticParameters = mutableMapOf<String, Any>()
    do {
        staticParameters["page"] = pageStart
        val giteaResponse = function.invoke(staticParameters)
        val currentPartEntities = giteaResponse.values
        val inFilter: Boolean = with(currentPartEntities.all(filter)) {
            entities += if (this) {
                currentPartEntities
            } else {
                currentPartEntities.filter(filter)
            }
            this
        }
        pageStart++
    } while ((giteaResponse.hasMore ?: (currentPartEntities.isNotEmpty())) && inFilter)
    return entities.toList()
}
