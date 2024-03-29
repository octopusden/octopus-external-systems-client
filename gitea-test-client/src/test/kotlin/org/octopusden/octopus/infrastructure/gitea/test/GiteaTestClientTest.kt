package org.octopusden.octopus.infrastructure.gitea.test

import java.io.File
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.CredentialProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.common.test.BaseTestClientTest
import org.octopusden.octopus.infrastructure.gitea.client.GiteaClassicClient
import org.octopusden.octopus.infrastructure.gitea.client.createPullRequestWithDefaultReviewers
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCommit
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaPullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaRepositoryConfig
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaTag
import org.octopusden.octopus.infrastructure.gitea.client.getBranches
import org.octopusden.octopus.infrastructure.gitea.client.getBranchesCommitGraph
import org.octopusden.octopus.infrastructure.gitea.client.getCommits
import org.octopusden.octopus.infrastructure.gitea.client.getTags


private const val HOST = "localhost:3000"
private const val USER = "test-admin"
private const val PASSWORD = "test-admin"

class GiteaTestClientTest :
    BaseTestClientTest(GiteaTestClient("http://$HOST", USER, PASSWORD), "ssh://git@$HOST:%s/%s.git") {

    private val client = GiteaClassicClient(object : ClientParametersProvider {
        override fun getApiUrl(): String = "http://$HOST"
        override fun getAuth(): CredentialProvider = StandardBasicCredCredentialProvider(USER, PASSWORD)
    })

    override fun getTags(project: String, repository: String) =
        client.getTags(project, repository).map { t -> t.toTestTag() }

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

    @Test
    fun testUpdateRepositoryConfiguration() {
        val organizationName = "test-edit-org"
        val repositoryName = "test-edit-repo"
        val newRepositoryName = "test-edit-repository"
        client.createOrganization(GiteaCreateOrganization(organizationName))
        client.createRepository(organizationName, GiteaCreateRepository("test-edit-repo"))
        client.updateRepositoryConfiguration(organizationName, repositoryName, GiteaRepositoryConfig(name = newRepositoryName))
        Assertions.assertEquals(client.getRepository(organizationName, newRepositoryName).name, newRepositoryName)
    }

    @Test
    fun testGetRepositoryConfiguration() {
        val organizationName = "test-get-org"
        val repositoryName = "test-get-repo"
        client.createOrganization(GiteaCreateOrganization(organizationName))
        client.createRepository(organizationName, GiteaCreateRepository(repositoryName))
        val repositoryConfig = client.getRepositoryConfiguration(organizationName, repositoryName)
        val newConfig = GiteaRepositoryConfig(repositoryConfig).copy(
            allowMergeCommits = repositoryConfig.allowMergeCommits?.let { !it },
            allowRebase = repositoryConfig.allowRebase?.let { !it },
            allowRebaseExplicit = repositoryConfig.allowRebaseExplicit?.let { !it },
            allowRebaseUpdate = repositoryConfig.allowRebaseUpdate?.let { !it },
            allowSquashMerge = repositoryConfig.allowSquashMerge?.let { !it },
            defaultAllowMaintainerEdit = repositoryConfig.defaultAllowMaintainerEdit?.let { !it },
            defaultBranch = repositoryConfig.defaultBranch?.let { "master" },
            defaultDeleteBranchAfterMerge = repositoryConfig.defaultDeleteBranchAfterMerge?.let { !it },
            defaultMergeStyle = repositoryConfig.defaultMergeStyle?.let { "squash" },
            description = repositoryConfig.description?.let { "Repository test get configuration" },
            hasIssues = repositoryConfig.hasIssues?.let { !it },
            hasProjects = repositoryConfig.hasProjects?.let { !it },
            hasPullRequests = repositoryConfig.hasPullRequests?.let { !it },
            hasWiki = repositoryConfig.hasWiki?.let { !it },
            ignoreWhitespaceConflicts = repositoryConfig.ignoreWhitespaceConflicts?.let { !it },
            name = repositoryConfig.name,
            private = repositoryConfig.private?.let { !it },
            template = repositoryConfig.template?.let { !it },
            website = repositoryConfig.website?.let { "https://localhost" },
        )
        client.updateRepositoryConfiguration(organizationName, repositoryName, newConfig)
        val resConfig = client.getRepositoryConfiguration(organizationName, repositoryName)
        Assertions.assertEquals(resConfig.allowMergeCommits, newConfig.allowMergeCommits)
        Assertions.assertEquals(resConfig.allowRebase, newConfig.allowRebase)
        Assertions.assertEquals(resConfig.allowRebaseExplicit, newConfig.allowRebaseExplicit)
        Assertions.assertEquals(resConfig.allowRebaseUpdate, newConfig.allowRebaseUpdate)
        Assertions.assertEquals(resConfig.allowSquashMerge, newConfig.allowSquashMerge)
        Assertions.assertEquals(resConfig.archived, newConfig.archived)
        Assertions.assertEquals(resConfig.defaultAllowMaintainerEdit, newConfig.defaultAllowMaintainerEdit)
        Assertions.assertEquals(resConfig.defaultBranch, newConfig.defaultBranch)
        Assertions.assertEquals(resConfig.defaultDeleteBranchAfterMerge, newConfig.defaultDeleteBranchAfterMerge)
        Assertions.assertEquals(resConfig.defaultMergeStyle, newConfig.defaultMergeStyle)
        Assertions.assertEquals(resConfig.description, newConfig.description)
        Assertions.assertEquals(resConfig.hasIssues, newConfig.hasIssues)
        Assertions.assertEquals(resConfig.hasProjects, newConfig.hasProjects)
        Assertions.assertEquals(resConfig.hasPullRequests, newConfig.hasPullRequests)
        Assertions.assertEquals(resConfig.hasWiki, newConfig.hasWiki)
        Assertions.assertEquals(resConfig.ignoreWhitespaceConflicts, newConfig.ignoreWhitespaceConflicts)
        Assertions.assertEquals(resConfig.mirrorInterval, newConfig.mirrorInterval)
        Assertions.assertEquals(resConfig.name, newConfig.name)
        Assertions.assertEquals(resConfig.private, newConfig.private)
        Assertions.assertEquals(resConfig.template, newConfig.template)
        Assertions.assertEquals(resConfig.website, newConfig.website)
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
}
