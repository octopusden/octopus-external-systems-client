package org.octopusden.octopus.infastructure.bitbucket.test

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketBasicCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClassicClient
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClientParametersProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.createPullRequestWithDefaultReviewers
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCommit
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCommitChange
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateTag
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketPullRequest
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketTag
import org.octopusden.octopus.infrastructure.bitbucket.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.bitbucket.client.getCommit
import org.octopusden.octopus.infrastructure.bitbucket.client.getCommitChanges
import org.octopusden.octopus.infrastructure.bitbucket.client.getCommits
import org.octopusden.octopus.infrastructure.bitbucket.client.getTag
import org.octopusden.octopus.infrastructure.bitbucket.client.getTags
import org.octopusden.octopus.infrastructure.common.test.BaseTestClient
import org.octopusden.octopus.infrastructure.common.test.BaseTestClientTest
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet
import java.nio.file.Path
import java.nio.file.Paths

private const val HOST = "localhost:7990"
private const val USER = "admin"
private const val PASSWORD = "admin"

class BitbucketTestClientTest : BaseTestClientTest(
    BitbucketTestClient("http://$HOST", USER, PASSWORD), "ssh://git@$HOST/%s/%s.git"
) {

    private val client = BitbucketClassicClient(object : BitbucketClientParametersProvider {
        override fun getApiUrl() = "http://$HOST"
        override fun getAuth(): BitbucketCredentialProvider = BitbucketBasicCredentialProvider(USER, PASSWORD)
    })

    override fun getTags(project: String, repository: String): Collection<TestTag> =
        client.getTags(project, repository).map { t -> t.toTestTag() }

    override fun getTag(project: String, repository: String, tag: String) =
        client.getTag(project, repository, tag).toTestTag()

    override fun deleteTag(project: String, repository: String, tag: String) =
        client.deleteTag(project, repository, tag)

    override fun createTag(project: String, repository: String, commitId: String, tag: String) {
        client.createTag(project, repository, BitbucketCreateTag(tag, commitId, "test"))
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

    @Test
    fun testGetRepositoryFiles() {
        val filesName = listOf("dummy.json", "dummy.txt")
        val paths: List<Path> = filesName.map { getTestResourceFile(it) }
        testClient.commit(
            NewChangeSet(
                "${BaseTestClient.DEFAULT_BRANCH} add dummy files",
                vcsUrl,
                BaseTestClient.DEFAULT_BRANCH
            ), null, paths
        )
        println("commit is successful!")
        val response = client.getRepositoryFiles(PROJECT, REPOSITORY, null, 100)
        println(response.values)
        Assertions.assertTrue(response.values.containsAll(filesName))
    }

    override fun getPullRequest(project: String, repository: String, index: Long) =
        client.getPullRequest(project, repository, index).toTestPullRequest()

    @Test
    fun testGetCommitInvalidId() {
        Assertions.assertThrowsExactly(NotFoundException::class.java, {
            client.getCommit(PROJECT, REPOSITORY, "bug/fix")
        }, "Ref 'bug/fix' does not exist in repository 'test-repository' and 'bug/fix' is not valid BitBucket commit id")
    }

    @Test
    fun testGetCommitChanges() {
        val changeSet = testClient.commit(
            NewChangeSet(
                "${BaseTestClient.DEFAULT_BRANCH} commit 1",
                vcsUrl,
                BaseTestClient.DEFAULT_BRANCH
            )
        )
        val changes = client.getCommitChanges(PROJECT, REPOSITORY, changeSet.id)
        Assertions.assertEquals(1, changes.size)
        Assertions.assertEquals(BitbucketCommitChange.BitbucketCommitChangeType.ADD, changes.first().type)
        Assertions.assertTrue(changes.first().path.value.endsWith(".commit"))
    }

    private fun BitbucketTag.toTestTag() = TestTag(displayId, latestCommit)
    private fun BitbucketCommit.toTestCommit() = TestCommit(id, message)
    private fun BitbucketPullRequest.toTestPullRequest() = TestPullRequest(id, title, description ?: "", fromRef.displayId, toRef.displayId)

    private fun getTestResourceFile(fileName: String): Path {
        val resource = javaClass.getResource("/$fileName") // Note the leading slash
        return if (resource != null) {
            Paths.get(resource.toURI())  // Returns Path directly
        } else {
            throw IllegalArgumentException("Resource file not found: $fileName")
        }
    }
}
