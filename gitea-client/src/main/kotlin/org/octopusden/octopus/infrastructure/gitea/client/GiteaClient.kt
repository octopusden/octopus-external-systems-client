package org.octopusden.octopus.infrastructure.gitea.client

import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import java.util.Date
import java.util.LinkedList
import org.octopusden.octopus.infrastructure.gitea.client.dto.BaseGiteaEntity
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaBranch
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCommit
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreatePullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateTag
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaEditRepoOption
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaEntityList
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaPullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaPullRequestReview
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaTag
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
        @Param("sha") sha: String,
        @QueryMap requestParams: Map<String, Any>
    ): GiteaCommit

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/tags")
    fun getTags(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @QueryMap requestParams: Map<String, Any>,
    ): GiteaEntityList<GiteaTag>

    @RequestLine("POST $REPO_PATH/{organization}/{repository}/tags")
    @Headers("Content-Type: application/json")
    fun createTag(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        dto: GiteaCreateTag
    ): GiteaTag

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/tags/{tag}", decodeSlash = false)
    @Throws(NotFoundException::class)
    fun getTag(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @Param("tag") tag: String
    ): GiteaTag

    @RequestLine("DELETE $REPO_PATH/{organization}/{repository}/tags/{tag}", decodeSlash = false)
    @Throws(NotFoundException::class)
    fun deleteTag(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @Param("tag") tag: String
    )

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/branches")
    fun getBranches(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @QueryMap requestParams: Map<String, Any>,
    ): GiteaEntityList<GiteaBranch>?

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/branches/{branch}")
    @Throws(NotFoundException::class)
    fun getBranch(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @Param("branch") branch: String
    ): GiteaBranch

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

    @RequestLine("GET $REPO_PATH/{organization}/{repository}/pulls/{number}/reviews")
    fun getPullRequestReviews(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        @Param("number") number: Long,
        @QueryMap requestParams: Map<String, Any>
    ): GiteaEntityList<GiteaPullRequestReview>

    @RequestLine("PATCH $REPO_PATH/{organization}/{repository}")
    @Headers("Content-Type: application/json")
    fun updateRepositoryConfiguration(
        @Param("organization") organization: String,
        @Param("repository") repository: String,
        dto: GiteaEditRepoOption
    )
}

fun GiteaClient.getOrganizations(): Collection<GiteaOrganization> {
    return execute({ parameters: Map<String, Any> -> getOrganizations(parameters) })
}
@Suppress("unused")
fun GiteaClient.getRepositories(organization: String): Collection<GiteaRepository> =
    execute({ parameters: Map<String, Any> -> getRepositories(organization, parameters) })

fun GiteaClient.getCommit(
    organization: String,
    repository: String,
    sha: String,
    files: Boolean = false
) = getCommit(
    organization, repository, sha, mapOf(
        "stat" to false, "verification" to false, "files" to files
    )
)

fun GiteaClient.getCommits(
    organization: String,
    repository: String,
    until: String,
    sinceDate: Date? = null,
    files: Boolean = false
) = execute({ parameters: Map<String, Any> ->
    getCommits(
        organization, repository, parameters + mapOf(
            "stat" to false, "verification" to false, "files" to files, "sha" to until
        )
    )
}, { commit: GiteaCommit -> sinceDate == null || commit.created > sinceDate })

@Suppress("unused")
fun GiteaClient.getCommits(
    organization: String,
    repository: String,
    until: String,
    since: String,
    files: Boolean = false
): List<GiteaCommit> {
    val toSha = getCommit(organization, repository, until).sha
    val fromSha = getCommit(organization, repository, since).sha
    if (fromSha == toSha) {
        return emptyList()
    }
    val parameters = mapOf(
        "limit" to ENTITY_LIMIT, "stat" to false, "verification" to false, "files" to files, "sha" to toSha
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

fun GiteaClient.getBranchesCommitGraph(organization: String, repository: String, files: Boolean = false)
        : GiteaCommitGraphSequence {
    val parameters = mapOf("limit" to ENTITY_LIMIT, "stat" to false, "verification" to false, "files" to files)
    return GiteaCommitGraphSequence(getBranches(organization, repository)) { branch, page ->
        getCommits(organization, repository, parameters + mapOf<String, Any>("sha" to branch, "page" to page))
    }
}

/**
 * Non Thread Safe implementation
 */
class GiteaCommitGraphSequence(
    branches: Collection<GiteaBranch>,
    pageRequest: (branch: String, page: Int) -> GiteaEntityList<GiteaCommit>
) : Sequence<GiteaCommit> {
    private val iterator = GiteaCommitGraphIterator(branches, pageRequest)

    override operator fun iterator() = iterator

    fun getVisited(): Set<String> = iterator.visited

    class GiteaCommitGraphIterator(
        branches: Collection<GiteaBranch>,
        private val pageRequest: (branch: String, page: Int) -> GiteaEntityList<GiteaCommit>
    ) : Iterator<GiteaCommit> {
        private var commitPage: Int = 0
        private val branchBuffer = LinkedList(branches)
        private val commitBuffer = LinkedList<GiteaCommit>()
        private var currentBranch: GiteaBranch? = branchBuffer.poll()
        private var orphanedCommits = emptyList<GiteaCommit>()
        val visited = mutableSetOf<String>()

        init {
            fetch()
        }

        override fun hasNext(): Boolean = commitBuffer.isNotEmpty()

        override fun next(): GiteaCommit = commitBuffer.pop().also { if (commitBuffer.isEmpty()) fetch() }

        private fun fetch() {
            while (currentBranch != null && commitBuffer.isEmpty()) {
                val currentBranchSha = currentBranch!!.commit.id
                val giteaResponse = pageRequest(currentBranchSha, ++commitPage)
                val includedCommits = giteaResponse.values.filter { !visited.contains(it.sha) }
                visited.addAll(includedCommits.map { it.sha })
                commitBuffer.addAll(includedCommits)
                orphanedCommits = (orphanedCommits + includedCommits).filter { commit ->
                    commit.parents.any { !visited.contains(it.sha) }
                }
                if (giteaResponse.hasMore == false || giteaResponse.values.isEmpty() || orphanedCommits.isEmpty()) {
                    _log.debug("Branch commits pages retrieved: ${currentBranch!!.name}:$commitPage")
                    currentBranch = branchBuffer.poll()
                    orphanedCommits = emptyList()
                    commitPage = 0
                }
            }
        }
    }
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
    description: String,
    assignee: String? = null
): GiteaPullRequest {
    val head = getBranch(organization, repository, sourceBranch).name
    val base = getBranch(organization, repository, targetBranch).name
    return createPullRequest(
        organization,
        repository,
        GiteaCreatePullRequest(title, description, head, base, assignee)
    )
}

@Suppress("unused")
fun GiteaClient.getPullRequests(
    organization: String,
    repository: String
) = execute({ parameters: Map<String, Any> ->
    getPullRequests(organization, repository, parameters)
})

@Suppress("unused")
fun GiteaClient.getPullRequestReviews(
    organization: String,
    repository: String,
    number: Long
) = execute({ parameters: Map<String, Any> ->
    getPullRequestReviews(organization, repository, number, parameters)
})

private fun <T : BaseGiteaEntity> execute(
    function: (Map<String, Any>) -> GiteaEntityList<T>,
    filter: (element: T) -> Boolean = { true }
): MutableList<T> {
    var page = 1
    val entities = mutableListOf<T>()
    val parameters = mutableMapOf<String, Any>()
    parameters["limit"] = ENTITY_LIMIT
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

fun GiteaRepository.toGiteaEditRepoOption(): GiteaEditRepoOption {
    return GiteaEditRepoOption(
        allowMergeCommits = allowMergeCommits,
        allowRebase = allowRebase,
        allowRebaseExplicit = allowRebaseExplicit,
        allowRebaseUpdate = allowRebaseUpdate,
        allowSquashMerge = allowSquashMerge,
        archived = archived,
        defaultAllowMaintainerEdit = defaultAllowMaintainerEdit,
        defaultBranch = defaultBranch,
        defaultDeleteBranchAfterMerge = defaultDeleteBranchAfterMerge,
        defaultMergeStyle = defaultMergeStyle,
        description = description,
        externalTracker = externalTracker,
        externalWiki = externalWiki,
        hasIssues = hasIssues,
        hasProjects = hasProjects,
        hasPullRequests = hasPullRequests,
        hasWiki = hasWiki,
        ignoreWhitespaceConflicts = ignoreWhitespaceConflicts,
        internalTracker = internalTracker,
        mirrorInterval = mirror?.let { m ->
            if (m) {
                mirrorInterval
            } else {
                null
            }
        },
        name = name,
        private = private,
        template = template,
        website = website
    )
}
