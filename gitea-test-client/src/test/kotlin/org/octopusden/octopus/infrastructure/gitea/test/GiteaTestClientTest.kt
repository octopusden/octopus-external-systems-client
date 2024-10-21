package org.octopusden.octopus.infrastructure.gitea.test

import java.io.File
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.CredentialProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.common.test.BaseTestClient
import org.octopusden.octopus.infrastructure.common.test.BaseTestClientTest
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet
import org.octopusden.octopus.infrastructure.common.util.RetryOperation
import org.octopusden.octopus.infrastructure.gitea.client.GiteaClassicClient
import org.octopusden.octopus.infrastructure.gitea.client.toGiteaEditRepoOption
import org.octopusden.octopus.infrastructure.gitea.client.createPullRequestWithDefaultReviewers
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCommit
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateTag
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaPullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaEditRepoOption
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaTag
import org.octopusden.octopus.infrastructure.gitea.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.gitea.client.getBranches
import org.octopusden.octopus.infrastructure.gitea.client.getBranchesCommitGraph
import org.octopusden.octopus.infrastructure.gitea.client.getCommit
import org.octopusden.octopus.infrastructure.gitea.client.getCommits
import org.octopusden.octopus.infrastructure.gitea.client.getTags
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit


private const val HOST = "localhost:3000"
private const val USER = "test-admin"
private const val PASSWORD = "test-admin"
private const val RETRY_INTERVAL_SEC: Long = 60
private const val RETRY_COUNT = 3

class GiteaTestClientTest :
    BaseTestClientTest(GiteaTestClient("http://$HOST", USER, PASSWORD), "ssh://git@$HOST:%s/%s.git") {

    private val log: Logger = LoggerFactory.getLogger(GiteaTestClientTest::class.java)
    private val client = GiteaClassicClient(object : ClientParametersProvider {
        override fun getApiUrl(): String = "http://$HOST"
        override fun getAuth(): CredentialProvider = StandardBasicCredCredentialProvider(USER, PASSWORD)
    })

    override fun getTags(project: String, repository: String) =
        client.getTags(project, repository).map { t -> t.toTestTag() }

    override fun getTag(project: String, repository: String, tag: String) =
        client.getTag(project, repository, tag).toTestTag()

    override fun deleteTag(project: String, repository: String, tag: String) {
        doWithRetries { client.deleteTag(project, repository, tag)  }
    }

    override fun createTag(project: String, repository: String, commitId: String, tag: String) {
        client.createTag(project, repository, GiteaCreateTag(tag, commitId, "test"))
    }

    override fun getCommits(project: String, repository: String, branch: String) =
        client.getCommits(project, repository, branch).map { c -> c.toTestCommit() }

    override fun createPullRequestWithDefaultReviewers(
        project: String,
        repository: String,
        sourceBranch: String,
        targetBranch: String,
        title: String,
        description: String
    ) = client.createPullRequestWithDefaultReviewers(
            project,
            repository,
            sourceBranch,
            targetBranch,
            title,
            description
        ).toTestPullRequest()

    override fun getPullRequest(project: String, repository: String, index: Long) =
        client.getPullRequest(project, repository, index).toTestPullRequest()

    private fun GiteaTag.toTestTag() = TestTag(name, commit.sha)
    private fun GiteaCommit.toTestCommit() = TestCommit(sha, commit.message)
    private fun GiteaPullRequest.toTestPullRequest() = TestPullRequest(number, title, body, head.label, base.label)
    private fun doWithRetries(retryFunction: () -> Unit) {
        return RetryOperation.configure<Unit> {
            attempts = RETRY_COUNT
            failureException { e ->
                NotFoundException::class.java == e.javaClass
            }
            onException { e, a ->
                val message = "attempt=$a ($RETRY_COUNT) is failed on $e"
                log.warn(message, e)
                message
            }
            executeOnFail {
                log.warn("Waiting $RETRY_INTERVAL_SEC seconds before retry")
                TimeUnit.SECONDS.sleep(RETRY_INTERVAL_SEC)
            }
        }.execute(retryFunction)
    }

    @Test
    fun testUpdateRepositoryConfiguration() {
        val organizationName = "test-edit-org"
        val repositoryName = "test-edit-repo"
        val newRepositoryName = "test-edit-repository"
        client.createOrganization(GiteaCreateOrganization(organizationName))
        client.createRepository(organizationName, GiteaCreateRepository("test-edit-repo"))
        client.updateRepositoryConfiguration(organizationName, repositoryName, GiteaEditRepoOption(name = newRepositoryName))
        Assertions.assertEquals(client.getRepository(organizationName, newRepositoryName).name, newRepositoryName)
    }

    @Test
    fun testGetRepository() {
        val organizationName = "test-get-org"
        val repositoryName = "test-get-repo"
        client.createOrganization(GiteaCreateOrganization(organizationName))
        client.createRepository(organizationName, GiteaCreateRepository(repositoryName))
        val giteaRepository = client.getRepository(organizationName, repositoryName)
        val giteaRepositoryConfig = giteaRepository.toGiteaEditRepoOption().copy(
            allowMergeCommits = giteaRepository.allowMergeCommits?.let { !it },
            allowRebase = giteaRepository.allowRebase?.let { !it },
            allowRebaseExplicit = giteaRepository.allowRebaseExplicit?.let { !it },
            allowRebaseUpdate = giteaRepository.allowRebaseUpdate?.let { !it },
            allowSquashMerge = giteaRepository.allowSquashMerge?.let { !it },
            defaultBranch = giteaRepository.defaultBranch?.let { "master" },
            description = giteaRepository.description?.let { "Repository test get configuration" },
            hasProjects = giteaRepository.hasProjects?.let { !it },
            hasPullRequests = giteaRepository.hasPullRequests?.let { !it },
            hasWiki = giteaRepository.hasWiki?.let { !it },
            internalTracker = giteaRepository.internalTracker,
            name = giteaRepository.name,
            private = giteaRepository.private?.let { !it },
            template = giteaRepository.template?.let { !it },
            website = giteaRepository.website?.let { "https://localhost" },
        )
        client.updateRepositoryConfiguration(organizationName, repositoryName, giteaRepositoryConfig)
        val giteaRepositoryConfigResult = client.getRepository(organizationName, repositoryName).toGiteaEditRepoOption()
        Assertions.assertEquals(giteaRepositoryConfigResult.toString(), giteaRepositoryConfig.toString())
    }

    @Test
    fun testGetBranchesCommitGraph() {
        val repository = "test-repository-branches-commit-graph"
        val vcsUrl = vcsFormatter.format(PROJECT, repository)
        testClient.importRepository(vcsUrl, File("src/test/resources/$repository.zip"))
        Assertions.assertIterableEquals(
            mutableSetOf<TestCommit>().apply {
                client.getBranches(PROJECT, repository).forEach { branch ->
                    addAll(testClient.getCommits(vcsUrl, branch.name).map { TestCommit(it.id, it.message) })
                }
            }.sortedBy { it.commitId },
            client.getBranchesCommitGraph(PROJECT, repository).map { it.toTestCommit() }.sortedBy { it.commitId }
        )
    }

    @Test
    fun getCommitWithFiles() {
        val changeSet = testClient.commit(
            NewChangeSet(
                "${BaseTestClient.DEFAULT_BRANCH} commit 1",
                vcsUrl,
                BaseTestClient.DEFAULT_BRANCH
            )
        )
        val commit = client.getCommit(PROJECT, REPOSITORY, changeSet.id, true)
        Assertions.assertEquals(1, commit.files!!.size)
        Assertions.assertEquals(GiteaCommit.GiteaCommitAffectedFileStatus.ADDED, commit.files!!.first().status)
        Assertions.assertTrue(commit.files!!.first().filename.endsWith(".commit"))
        Assertions.assertEquals(commit, client.getCommits(PROJECT, REPOSITORY, changeSet.id, null, true).first())
    }
}
