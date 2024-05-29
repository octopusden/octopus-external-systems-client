package org.octopusden.octopus.infrastructure.bitbucket.client

import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import java.util.Date
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BaseBitbucketEntity
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketUser
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketBranch
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCommit
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCommitChange
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreatePrRef
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateProject
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreatePullRequest
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreatePullRequestReviewer
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateRepository
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketEntityList
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketJiraCommit
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketProject
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketPullRequest
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketRepository
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketTag
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketUpdateRepository
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.DefaultReviewersQuery
import org.octopusden.octopus.infrastructure.bitbucket.client.exception.InvalidCommitIdException
import org.octopusden.octopus.infrastructure.bitbucket.client.exception.NotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val _log: Logger = LoggerFactory.getLogger(BitbucketClient::class.java)

const val PROJECT_PATH = "rest/api/1.0/projects"
const val REPO_PATH = "rest/api/1.0/repos"
const val JIRA_ISSUES_PATH = "rest/jira/1.0/issues"
const val DEFAULT_REVIEWERS_PATH = "rest/default-reviewers/1.0/projects"
const val ENTITY_LIMIT = 100

interface BitbucketClient {

    @RequestLine("GET $PROJECT_PATH")
    fun getProjects(@QueryMap requestParams: Map<String, Any>): BitbucketEntityList<BitbucketProject>

    @RequestLine("GET $REPO_PATH")
    fun getRepositories(@QueryMap requestParams: Map<String, Any>): BitbucketEntityList<BitbucketRepository>

    @RequestLine("POST $PROJECT_PATH")
    @Headers("Content-Type: application/json")
    fun createProject(dto: BitbucketCreateProject)

    @RequestLine("GET $PROJECT_PATH/{projectKey}")
    @Throws(NotFoundException::class)
    fun getProject(@Param("projectKey") projectKey: String): BitbucketProject

    @RequestLine("GET $PROJECT_PATH/{projectKey}/repos")
    fun getRepositories(
        @Param("projectKey") projectKey: String,
        @QueryMap requestParams: Map<String, Any>
    ): BitbucketEntityList<BitbucketRepository>

    @RequestLine("GET $PROJECT_PATH/{projectKey}/repos/{repository}")
    @Throws(NotFoundException::class)
    fun getRepository(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String
    ): BitbucketRepository

    @RequestLine("POST $PROJECT_PATH/{projectKey}/repos")
    @Headers("Content-Type: application/json")
    fun createRepository(@Param("projectKey") projectKey: String, dto: BitbucketCreateRepository)

    @RequestLine("PUT $PROJECT_PATH/{projectKey}/repos/{repository}")
    @Headers("Content-Type: application/json")
    fun updateRepository(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String,
        dto: BitbucketUpdateRepository
    )

    @RequestLine("DELETE $PROJECT_PATH/{projectKey}/repos/{repository}")
    fun deleteRepository(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String,
    )

    @RequestLine("GET $PROJECT_PATH/{projectKey}/repos/{repository}/commits")
    fun getCommits(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String,
        @QueryMap requestParams: Map<String, Any>
    ): BitbucketEntityList<BitbucketCommit>

    @RequestLine("GET $PROJECT_PATH/{projectKey}/repos/{repository}/commits/{id}")
    @Throws(NotFoundException::class)
    @Suppress("FunctionName")
    fun _getCommit(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String,
        @Param("id", expander = BitbucketCommitIdValidator::class) id: String
    ): BitbucketCommit

    @RequestLine("GET $PROJECT_PATH/{projectKey}/repos/{repository}/commits/{id}/changes")
    @Throws(NotFoundException::class)
    @Suppress("FunctionName")
    fun _getCommitChanges(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String,
        @Param("id", expander = BitbucketCommitIdValidator::class) id: String
    ): BitbucketEntityList<BitbucketCommitChange>

    @RequestLine("GET $JIRA_ISSUES_PATH/{issueKey}/commits")
    fun getCommits(
        @Param("issueKey") issueKey: String,
        @QueryMap requestParams: Map<String, Any>
    ): BitbucketEntityList<BitbucketJiraCommit>

    @RequestLine("GET $PROJECT_PATH/{projectKey}/repos/{repository}/tags")
    fun getTags(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String,
        @QueryMap requestParams: Map<String, Any>,
    ): BitbucketEntityList<BitbucketTag>

    @RequestLine("GET $PROJECT_PATH/{projectKey}/repos/{repository}/branches")
    fun getBranches(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String,
        @QueryMap requestParams: Map<String, Any>,
    ): BitbucketEntityList<BitbucketBranch>

    @RequestLine("GET $DEFAULT_REVIEWERS_PATH/{projectKey}/repos/{repository}/reviewers")
    fun getDefaultReviewers(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String,
        @QueryMap query: DefaultReviewersQuery
    ): Set<BitbucketUser>

    @RequestLine("POST $PROJECT_PATH/{projectKey}/repos/{repository}/pull-requests")
    @Headers("Content-Type: application/json")
    fun createPullRequest(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String,
        dto: BitbucketCreatePullRequest
    ): BitbucketPullRequest

    @RequestLine("GET $PROJECT_PATH/{projectKey}/repos/{repository}/pull-requests/{id}")
    @Headers("Content-Type: application/json")
    fun getPullRequest(
        @Param("projectKey") projectKey: String,
        @Param("repository") repository: String,
        @Param("id") id: Long
    ): BitbucketPullRequest
}

fun BitbucketClient.getProjects(): List<BitbucketProject> {
    return execute({ parameters: Map<String, Any> -> getProjects(parameters) })
}

fun BitbucketClient.getRepositories(): List<BitbucketRepository> =
    execute({ parameters: Map<String, Any> -> getRepositories(parameters) })

fun BitbucketClient.getRepositories(projectKey: String): List<BitbucketRepository> =
    execute({ parameters: Map<String, Any> -> getRepositories(projectKey, parameters) })

private fun BitbucketClient.findRef(projectKey: String, repository: String, ref: String) =
    if (ref.startsWith("refs/heads/")) {
        getBranches(projectKey, repository).firstOrNull { branch -> branch.id == ref }
    } else if (ref.startsWith("refs/tags/")) {
        getTags(projectKey, repository).firstOrNull { tag -> tag.id == ref }
    } else {
        getBranches(projectKey, repository).firstOrNull { branch -> branch.id == "refs/heads/$ref" }
            ?: getTags(projectKey, repository).firstOrNull { tag -> tag.id == "refs/tags/$ref" }
    }

fun BitbucketClient.getCommit(projectKey: String, repository: String, commitIdOrRef: String): BitbucketCommit =
    try {
        _getCommit(projectKey, repository, commitIdOrRef)
    } catch (e: InvalidCommitIdException) {
        _log.info("Treat `$commitIdOrRef` as a ref. ${e.message}")
        findRef(projectKey, repository, commitIdOrRef)?.let {
            _getCommit(projectKey, repository, it.latestCommit)
        } ?: throw NotFoundException("Ref '$commitIdOrRef' does not exist in repository '$repository' and ${e.message}")
    }

fun BitbucketClient.getCommitChanges(projectKey: String, repository: String, commitIdOrRef: String): List<BitbucketCommitChange> =
    try {
        _getCommitChanges(projectKey, repository, commitIdOrRef)
    } catch (e: InvalidCommitIdException) {
        _log.info("Treat `$commitIdOrRef` as a ref. ${e.message}")
        findRef(projectKey, repository, commitIdOrRef)?.let {
            _getCommitChanges(projectKey, repository, it.latestCommit)
        } ?: throw NotFoundException("Ref '$commitIdOrRef' does not exist in repository '$repository' and ${e.message}")
    }.values

fun BitbucketClient.getCommits(
    projectKey: String,
    repository: String,
    until: String,
    since: String
): List<BitbucketCommit> {
    val toId = getCommit(projectKey, repository, until).id
    val fromId = getCommit(projectKey, repository, since).id
    return if (toId == fromId) {
        emptyList()
    } else {
        execute({ parameters: Map<String, Any> ->
            getCommits(projectKey, repository, parameters + mapOf("until" to toId, "since" to fromId))
        }).also { commits ->
            if (!commits.any { commit -> commit.parents.any { it.id == fromId } }) {
                throw NotFoundException("Cannot find commit '$fromId' in commit graph for commit '$toId' in '$projectKey:$repository'")
            }
        }
    }
}

fun BitbucketClient.getCommits(
    projectKey: String, repository: String, until: String, sinceDate: Date? = null
) = execute({ parameters: Map<String, Any> ->
    getCommits(projectKey, repository, parameters + mapOf("until" to until))
}, { commit: BitbucketCommit ->
    sinceDate == null || commit.authorTimestamp > sinceDate
})

fun BitbucketClient.getCommits(issueKey: String): List<BitbucketJiraCommit> =
    execute({ parameters: Map<String, Any> -> getCommits(issueKey, parameters) })

fun BitbucketClient.getTags(
    projectKey: String,
    repository: String
): List<BitbucketTag> = execute({ parameters: Map<String, Any> -> getTags(projectKey, repository, parameters) })

fun BitbucketClient.getBranches(
    projectKey: String,
    repository: String
): List<BitbucketBranch> = execute({ parameters: Map<String, Any> -> getBranches(projectKey, repository, parameters) })

fun BitbucketClient.createPullRequestWithDefaultReviewers(
    projectKey: String,
    repository: String,
    sourceBranch: String,
    targetBranch: String,
    title: String,
    description: String
): BitbucketPullRequest {
    val existedRepository = getRepository(projectKey, repository)

    val branches = getBranches(projectKey, repository)
        .associateBy { bitbucketBranch -> bitbucketBranch.displayId }

    fun getRef(type: String, branchName: String): BitbucketCreatePrRef = branches[branchName]
        ?.let { BitbucketCreatePrRef(it.id, existedRepository) }
        ?: throw NotFoundException("$type branch '$branchName' not found in '$projectKey:$repository'")

    val sourceRef = getRef("Source", sourceBranch)
    val targetRef = getRef("Target", targetBranch)

    val defaultReviewers = getDefaultReviewers(
        projectKey,
        repository,
        DefaultReviewersQuery(existedRepository.id, sourceRef.id, existedRepository.id, targetRef.id)
    )
        .map { BitbucketCreatePullRequestReviewer(it) }
        .toSet()

    return createPullRequest(
        projectKey,
        repository,
        BitbucketCreatePullRequest(title, description, sourceRef, targetRef, defaultReviewers)
    )
}

private fun <T : BaseBitbucketEntity<*>> execute(
    function: (Map<String, Any>) -> BitbucketEntityList<T>,
    filter: (element: T) -> Boolean = { true }
): List<T> {
    var page = 0
    var pageStart = 0
    val entities = mutableListOf<T>()
    val parameters = mutableMapOf<String, Any>("limit" to ENTITY_LIMIT)
    do {
        page++
        parameters["start"] = pageStart
        val currentPartEntities = function.invoke(parameters)
        val inFilter: Boolean = with(currentPartEntities.values.all(filter)) {
            entities += if (this) {
                currentPartEntities.values
            } else {
                currentPartEntities.values.filter(filter)
            }
            this
        }
        pageStart = currentPartEntities.nextPageStart ?: pageStart
    } while (!currentPartEntities.isLastPage && inFilter)
    _log.debug("Pages retrieved: $page")
    return entities
}
