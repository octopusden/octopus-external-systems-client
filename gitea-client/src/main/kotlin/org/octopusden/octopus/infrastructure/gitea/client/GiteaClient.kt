package org.octopusden.octopus.infrastructure.gitea.client

import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import java.util.Date
import org.octopusden.octopus.infrastructure.gitea.client.dto.BaseGiteaEntity
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaBranch
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCommit
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreatePullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaRepositoryConfig
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaEntityList
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaPullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaTag
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaUser
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaGetRepositoryConfig
import org.octopusden.octopus.infrastructure.gitea.client.exception.NotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val _log: Logger = LoggerFactory.getLogger(GiteaClient::class.java)

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

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/pulls")
    fun getPullRequests(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @QueryMap requestParams: Map<String, Any>
    ): GiteaEntityList<GiteaPullRequest>

    @RequestLine("POST $REPO_PATH/{organization}/{repository}/pulls")
    @Headers("Content-Type: application/json")
    fun createPullRequest(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        dto: GiteaCreatePullRequest
    ): GiteaPullRequest

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/pulls/{number}")
    fun getPullRequest(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @Param("number") number: Long
    ): GiteaPullRequest

    @RequestLine("PATCH $REPO_PATH/{organization}/{repository}")
    @Headers("Content-Type: application/json")
    fun updateRepositoryConfiguration(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        dto: GiteaRepositoryConfig
    )

    @RequestLine("GET $REPO_PATH/{organization}/{repository}")
    @Headers("Content-Type: application/json")
    fun getRepositoryConfiguration(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
    ): GiteaGetRepositoryConfig
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
    until: String,
    sinceDate: Date? = null
) = execute({ parameters: Map<String, Any> ->
    getCommits(
        organization, repository, parameters + mapOf(
            "limit" to ENTITY_LIMIT, "stat" to false, "verification" to false, "files" to false, "sha" to until
        )
    )
}, { commit: GiteaCommit -> sinceDate == null || commit.created > sinceDate })

fun GiteaClient.getCommits(
    organization: String,
    repository: String,
    until: String,
    since: String
): List<GiteaCommit> {
    val toSha = getCommit(organization, repository, until).sha
    val fromSha = getCommit(organization, repository, since).sha
    if (fromSha == toSha) {
        return emptyList()
    }
    val parameters = mapOf(
        "limit" to ENTITY_LIMIT, "stat" to false, "verification" to false, "files" to false, "sha" to toSha
    )
    val commits = mutableMapOf<String, GiteaCommit>()
    var page = 0
    var sinceCommitFound = false
    var orphanedCommits = listOf<GiteaCommit>()
    val excludedCommits = mutableSetOf<String>()
    do {
        val giteaResponse = getCommits(organization, repository, parameters + mapOf("page" to ++page))
        val includedCommits = mutableListOf<GiteaCommit>()
        for (commit in giteaResponse.values) {
            if (commit.sha == fromSha) {
                sinceCommitFound = true
                excludedCommits.add(commit.sha)
            }
            if (excludedCommits.contains(commit.sha)) {
                excludedCommits.addAll(commit.parents.map { it.sha })
            } else {
                includedCommits.add(commit)
            }
        }
        commits.putAll(includedCommits.associateBy { it.sha })
        orphanedCommits = (orphanedCommits + includedCommits).filter { commit ->
            commit.parents.any { parentCommit ->
                !excludedCommits.contains(parentCommit.sha) &&
                        !commits.containsKey(parentCommit.sha)
            }
        }
    } while ((giteaResponse.hasMore ?: (giteaResponse.values.isNotEmpty())) && orphanedCommits.isNotEmpty())
    _log.debug("Pages retrieved: $page")
    if (!sinceCommitFound) {
        throw NotFoundException("Cannot find commit '$fromSha' in commit graph for commit '$toSha' in '$organization:$repository'")
    }
    return commits.map { it.value }
}

fun GiteaClient.getBranchesCommitGraph(
    organization: String,
    repository: String
): List<GiteaCommit> {
    val parameters = mapOf("limit" to ENTITY_LIMIT, "stat" to false, "verification" to false, "files" to false)
    val commits = mutableMapOf<String, GiteaCommit>()
    var page: Int
    getBranches(organization, repository).forEach { branch ->
        var orphanedCommits = listOf<GiteaCommit>()
        page = 0
        do {
            val giteaResponse = getCommits(organization, repository, parameters + mapOf("sha" to branch.commit.id, "page" to ++page))
            val includedCommits = giteaResponse.values.filter { !commits.containsKey(it.sha) }
            commits.putAll(includedCommits.associateBy { it.sha })
            orphanedCommits = (orphanedCommits + includedCommits).filter { commit ->
                commit.parents.any { !commits.containsKey(it.sha) }
            }
        } while ((giteaResponse.hasMore ?: (giteaResponse.values.isNotEmpty())) && orphanedCommits.isNotEmpty())
        _log.debug("Pages retrieved: $page")
    }
    return commits.values.sortedByDescending { it.created }
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

fun GiteaClient.getPullRequests(
    organization: String,
    repository: String
) = execute({ parameters: Map<String, Any> ->
    getPullRequests(
        organization, repository, parameters + mapOf("limit" to ENTITY_LIMIT)
    )
})

private fun <T : BaseGiteaEntity> execute(
    function: (Map<String, Any>) -> GiteaEntityList<T>,
    filter: (element: T) -> Boolean = { true }
): MutableList<T> {
    var page = 1
    val entities = mutableListOf<T>()
    val parameters = mutableMapOf<String, Any>()
    do {
        parameters["page"] = page
        val giteaResponse = function.invoke(parameters)
        val currentPartEntities = giteaResponse.values
        val inFilter: Boolean = with(currentPartEntities.all(filter)) {
            entities += if (this) {
                currentPartEntities
            } else {
                currentPartEntities.filter(filter)
            }
            this
        }
        page++
    } while ((giteaResponse.hasMore ?: (currentPartEntities.isNotEmpty())) && inFilter)
    _log.debug("Pages retrieved: $page")
    return entities
}
