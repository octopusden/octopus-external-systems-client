package org.octopusden.octopus.infrastructure.common.test

import java.io.File
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTestClientTest(
    protected val testClient: TestClient, protected val vcsFormatter: String
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

    abstract fun getPullRequest(
        project: String,
        repository: String,
        index: Long
    ): TestPullRequest

    private val vcsUrl: String = vcsFormatter.format(PROJECT, REPOSITORY)

    @AfterEach
    fun afterEachTestClientTest() {
        testClient.clearData()
    }

    @Test
    fun testCommit() {
        testClient.commit(
            NewChangeSet(
                "${BaseTestClient.DEFAULT_BRANCH} commit 1",
                vcsUrl,
                BaseTestClient.DEFAULT_BRANCH
            )
        )
        testClient.commit(
            NewChangeSet(
                "${BaseTestClient.DEFAULT_BRANCH} commit 2",
                vcsUrl,
                BaseTestClient.DEFAULT_BRANCH
            )
        )
        val firstDevelopCommitId = testClient.commit(
            NewChangeSet(
                "$DEVELOP_BRANCH commit 1",
                vcsUrl,
                DEVELOP_BRANCH
            )
        )
        testClient.commit(
            NewChangeSet(
                "$DEVELOP_BRANCH commit 2",
                vcsUrl,
                DEVELOP_BRANCH
            )
        )
        testClient.commit(
            NewChangeSet(
                "$FEATURE_BRANCH commit 1",
                vcsUrl,
                FEATURE_BRANCH
            ),
            firstDevelopCommitId.id
        )

        checkCommits(
            BaseTestClient.DEFAULT_BRANCH,
            listOf(
                "${BaseTestClient.DEFAULT_BRANCH} commit 2",
                "${BaseTestClient.DEFAULT_BRANCH} commit 1",
                BaseTestClient.INITIAL_COMMIT_MESSAGE
            )
        )
        checkCommits(
            DEVELOP_BRANCH,
            listOf(
                "$DEVELOP_BRANCH commit 2",
                "$DEVELOP_BRANCH commit 1",
                "${BaseTestClient.DEFAULT_BRANCH} commit 2",
                "${BaseTestClient.DEFAULT_BRANCH} commit 1",
                BaseTestClient.INITIAL_COMMIT_MESSAGE
            )
        )
        checkCommits(
            FEATURE_BRANCH,
            listOf(
                "$FEATURE_BRANCH commit 1",
                "$DEVELOP_BRANCH commit 1",
                "${BaseTestClient.DEFAULT_BRANCH} commit 2",
                "${BaseTestClient.DEFAULT_BRANCH} commit 1",
                BaseTestClient.INITIAL_COMMIT_MESSAGE
            )
        )
    }

    @Test
    fun testCommitBranchExistsAndParentException() {
        assertThrows<IllegalArgumentException> {
            testClient.commit(NewChangeSet("message", vcsUrl, DEVELOP_BRANCH), "leftId")
        }
        val commitId = testClient.commit(NewChangeSet("message", vcsUrl, DEVELOP_BRANCH)).id
        assertThrows<IllegalArgumentException> {
            testClient.commit(NewChangeSet("message", vcsUrl, DEVELOP_BRANCH), commitId)
        }
    }

    @Test
    fun testTag() {
        val expectedId = testClient.commit(NewChangeSet("tag commit", vcsUrl, BaseTestClient.DEFAULT_BRANCH)).id
        testClient.tag(vcsUrl, expectedId, TAG)
        Assertions.assertEquals(TAG, getTags(PROJECT, REPOSITORY).first().displayId)
        Assertions.assertEquals(expectedId, getTags(PROJECT, REPOSITORY).first().commitId)
    }

    @Test
    fun testTagException() {
        assertThrows<IllegalArgumentException> {
            testClient.tag(vcsUrl, "", TAG)
        }
        testClient.commit(NewChangeSet("message", vcsUrl, DEVELOP_BRANCH)).id
        assertThrows<IllegalArgumentException> {
            testClient.tag(vcsUrl, "left", TAG)
        }
    }

    @Test
    fun testPullRequest() {
        testClient.commit(
            NewChangeSet(
                "${BaseTestClient.DEFAULT_BRANCH} commit",
                vcsUrl,
                BaseTestClient.DEFAULT_BRANCH
            )
        )
        testClient.commit(NewChangeSet("$FEATURE_BRANCH commit", vcsUrl, FEATURE_BRANCH))
        Thread.sleep(5000)
        val title = "PR Title"
        val description = "PR Description"
        val pullRequest = createPullRequestWithDefaultReviewers(
            PROJECT,
            REPOSITORY,
            FEATURE_BRANCH,
            BaseTestClient.DEFAULT_BRANCH,
            title,
            description
        )
        Thread.sleep(60000)
        Assertions.assertEquals(pullRequest.title, title)
        Assertions.assertEquals(pullRequest.description, description)
        Assertions.assertEquals(pullRequest.sourceBranch, FEATURE_BRANCH)
        Assertions.assertEquals(pullRequest.targetBranch, BaseTestClient.DEFAULT_BRANCH)
        Assertions.assertEquals(pullRequest, getPullRequest(PROJECT, REPOSITORY, pullRequest.index))
    }

    @Test
    fun testExportImport() {
        testClient.commit(NewChangeSet("${BaseTestClient.DEFAULT_BRANCH} commit", vcsUrl, BaseTestClient.DEFAULT_BRANCH))
        testClient.commit(NewChangeSet("$FEATURE_BRANCH commit", vcsUrl, FEATURE_BRANCH))
        val tagCommitId = testClient.commit(NewChangeSet("tag commit", vcsUrl, BaseTestClient.DEFAULT_BRANCH)).id
        testClient.tag(vcsUrl, tagCommitId, TAG)
        val zip = File.createTempFile("TestClientTest", "zip").also { it.deleteOnExit() }
        testClient.exportRepository(vcsUrl, zip)
        testClient.clearData()
        Assertions.assertThrows(Exception::class.java) {
            getCommits(PROJECT, REPOSITORY, BaseTestClient.DEFAULT_BRANCH)
        }
        Assertions.assertThrows(Exception::class.java) {
            getCommits(PROJECT, REPOSITORY, FEATURE_BRANCH)
        }
        Assertions.assertThrows(Exception::class.java) {
            getTags(PROJECT, REPOSITORY)
        }
        testClient.importRepository(vcsUrl, zip)
        checkCommits(
            BaseTestClient.DEFAULT_BRANCH,
            listOf(
                "tag commit",
                "${BaseTestClient.DEFAULT_BRANCH} commit",
                BaseTestClient.INITIAL_COMMIT_MESSAGE
            )
        )
        checkCommits(
            FEATURE_BRANCH,
            listOf(
                "$FEATURE_BRANCH commit",
                "${BaseTestClient.DEFAULT_BRANCH} commit",
                BaseTestClient.INITIAL_COMMIT_MESSAGE
            )
        )
        Assertions.assertEquals(TAG, getTags(PROJECT, REPOSITORY).first().displayId)
        Assertions.assertEquals(tagCommitId, getTags(PROJECT, REPOSITORY).first().commitId)
    }

    @Test
    fun testGetCommits() {
        testClient.commit(NewChangeSet("${BaseTestClient.DEFAULT_BRANCH} commit", vcsUrl, BaseTestClient.DEFAULT_BRANCH))
        testClient.commit(NewChangeSet("$FEATURE_BRANCH commit", vcsUrl, FEATURE_BRANCH), BaseTestClient.DEFAULT_BRANCH)
        testClient.commit(NewChangeSet("$DEVELOP_BRANCH commit", vcsUrl, DEVELOP_BRANCH), BaseTestClient.DEFAULT_BRANCH)
        val defaultBranchCommits = getCommits(PROJECT, REPOSITORY, BaseTestClient.DEFAULT_BRANCH).sortedBy { it.commitId }
        val featureBranchCommits = getCommits(PROJECT, REPOSITORY, FEATURE_BRANCH).sortedBy { it.commitId }
        val developBranchCommits = getCommits(PROJECT, REPOSITORY, DEVELOP_BRANCH).sortedBy { it.commitId }
        Assertions.assertIterableEquals(
            defaultBranchCommits,
            testClient.getCommits(vcsUrl, BaseTestClient.DEFAULT_BRANCH).map { TestCommit(it.id, it.message) }.sortedBy { it.commitId }
        )
        Assertions.assertIterableEquals(
            featureBranchCommits,
            testClient.getCommits(vcsUrl, FEATURE_BRANCH).map { TestCommit(it.id, it.message) }.sortedBy { it.commitId }
        )
        Assertions.assertIterableEquals(
            developBranchCommits,
            testClient.getCommits(vcsUrl, DEVELOP_BRANCH).map { TestCommit(it.id, it.message) }.sortedBy { it.commitId }
        )
        Assertions.assertIterableEquals(
            (defaultBranchCommits + featureBranchCommits + developBranchCommits).toSet().sortedBy { it.commitId },
            testClient.getCommits(vcsUrl).map { TestCommit(it.id, it.message) }.sortedBy { it.commitId }
        )
    }

    private fun checkCommits(branch: String, expected: List<String>) {
        Assertions.assertIterableEquals(
            expected,
            getCommits(PROJECT, REPOSITORY, branch).map { it.message }
        )
    }

    data class TestTag(val displayId: String, val commitId: String)
    data class TestCommit(val commitId: String, val message: String)
    data class TestPullRequest(val index: Long, val title: String, val description: String, val sourceBranch: String, val targetBranch: String)

    companion object {
        private const val TAG = "test_tag"
        const val PROJECT = "test_project"
        const val REPOSITORY = "test-repository"
        private const val FEATURE_BRANCH = "feature"
        private const val DEVELOP_BRANCH = "develop"
    }
}
