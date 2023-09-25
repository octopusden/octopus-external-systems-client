package org.octopusden.octopus.infrastructure.common.test

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet


private const val TAG = "test_tag"
private const val PROJECT = "test_project"
private const val REPOSITORY = "test-repository"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTestClientTest(
    private val testClient: TestClient, vcsFormatter: String
) {

    abstract fun getTags(project: String, repository: String): Collection<TestTag>
    abstract fun getCommits(project: String, repository: String, branch: String): Collection<TestCommit>
    abstract fun createPullRequestWithDefaultReviewers(
        project: String,
        repository: String,
        sourceBranch: String,
        targetBranch: String,
        title: String,
        description: String
    ): TestPullRequest

    private var vcsUrl: String = vcsFormatter.format(PROJECT, REPOSITORY)

    @AfterEach
    fun afterEachTestClientTest() {
        testClient.clearData()
    }

    @Test
    fun testCommit() {
        testClient.commit(
            NewChangeSet(
                "master commit 1",
                vcsUrl,
                "master"
            )
        )
        testClient.commit(
            NewChangeSet(
                "master commit 2",
                vcsUrl,
                "master"
            )
        )
        val firstDevelopCommitId = testClient.commit(
            NewChangeSet(
                "develop commit 1",
                vcsUrl,
                "develop"
            )
        )
        testClient.commit(
            NewChangeSet(
                "develop commit 2",
                vcsUrl,
                "develop"
            )
        )
        testClient.commit(
            NewChangeSet(
                "feature commit 1",
                vcsUrl,
                "feature"
            ),
            firstDevelopCommitId.id
        )

        checkCommits("master", listOf("master commit 2", "master commit 1", "initial commit"))
        checkCommits(
            "develop",
            listOf("develop commit 2", "develop commit 1", "master commit 2", "master commit 1", "initial commit")
        )
        checkCommits(
            "feature",
            listOf("feature commit 1", "develop commit 1", "master commit 2", "master commit 1", "initial commit")
        )
    }

    @Test
    fun testCommitBranchExistsAndParentException() {
        assertThrows<IllegalArgumentException> {
            testClient.commit(NewChangeSet("message", vcsUrl, "develop"), "leftId")
        }

        val commitId = testClient.commit(NewChangeSet("message", vcsUrl, "develop")).id
        assertThrows<IllegalArgumentException> {
            testClient.commit(NewChangeSet("message", vcsUrl, "develop"), commitId)
        }
    }

    @Test
    fun testTag() {
        val expectedId = testClient.commit(NewChangeSet("test tag commit", vcsUrl, "master")).id
        testClient.tag(vcsUrl, expectedId, TAG)
        Assertions.assertEquals(TAG, getTags(PROJECT, REPOSITORY).first().displayId)
        Assertions.assertEquals(expectedId, getTags(PROJECT, REPOSITORY).first().commitId)
    }

    @Test
    fun testTagException() {
        assertThrows<IllegalArgumentException> {
            testClient.tag(vcsUrl, "", TAG)
        }
        testClient.commit(NewChangeSet("message", vcsUrl, "develop")).id
        assertThrows<IllegalArgumentException> {
            testClient.tag(vcsUrl, "left", TAG)
        }
    }

    @Test
    fun testPullRequest() {
        val mainBranch = "master"
        testClient.commit(NewChangeSet("initial commit", vcsUrl, mainBranch))
        val featureBranch = "feature"
        testClient.commit(NewChangeSet("feature commit", vcsUrl, featureBranch))

        val pullRequest = createPullRequestWithDefaultReviewers(
            PROJECT,
            REPOSITORY,
            featureBranch,
            mainBranch,
            "PR Title",
            "PR Description"
        )
        Assertions.assertTrue(pullRequest.id > 0)
    }

    private fun checkCommits(branch: String, expected: List<String>) {
        Assertions.assertIterableEquals(
            expected,
            getCommits(PROJECT, REPOSITORY, branch).map { it.message }
        )
    }

    data class TestTag(val displayId: String, val commitId: String)
    data class TestCommit(val commitId: String, val message: String)
    data class TestPullRequest(val id: Long)
}
