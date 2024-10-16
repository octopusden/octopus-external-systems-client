package org.octopusden.octopus.infrastructure.common.test

import java.io.File
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet
import org.octopusden.octopus.infrastructure.common.util.RetryOperation
import org.octopusden.octopus.infrastructure.gitea.client.exception.NotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

private const val RETRY_INTERVAL_SEC: Long = 60
private const val RETRY_COUNT = 3

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTestClientTest(
    protected val testClient: TestClient, protected val vcsFormatter: String
) {
    abstract fun getTags(project: String, repository: String): Collection<TestTag>
    abstract fun getTag(project: String, repository: String, tag: String): TestTag
    abstract fun deleteTag(project: String, repository: String, tag: String)
    abstract fun createTag(project: String, repository: String, commitId: String, tag: String)
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

    private val _log: Logger = LoggerFactory.getLogger(BaseTestClientTest::class.java)
    protected val vcsUrl: String = vcsFormatter.format(PROJECT, REPOSITORY)

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
        val tag1 = TestTag("test-0.1", testClient.commit(NewChangeSet("tag commit 1", vcsUrl, BaseTestClient.DEFAULT_BRANCH)).id)
        val tag2 = TestTag("test-0.2", testClient.commit(NewChangeSet("tag commit 2", vcsUrl, BaseTestClient.DEFAULT_BRANCH)).id)
        testClient.tag(vcsUrl, tag1.commitId, tag1.displayId)
        createTag(PROJECT, REPOSITORY, tag2.commitId, tag2.displayId)
        Assertions.assertIterableEquals(listOf(tag1, tag2), getTags(PROJECT, REPOSITORY).sortedBy { it.displayId })
        Assertions.assertEquals(tag1, getTag(PROJECT, REPOSITORY, tag1.displayId))
        Assertions.assertEquals(tag2, getTag(PROJECT, REPOSITORY, tag2.displayId))
        RetryOperation.configure<Unit> {
            attempts = RETRY_COUNT
            failureException { e ->
                NotFoundException::class.java == e.javaClass
            }
            onException { e, a ->
                val message = "attempt=$a ($RETRY_COUNT) is failed on $e"
                _log.warn(message)
                message
            }
            executeOnFail {
                _log.warn("Waiting $RETRY_INTERVAL_SEC seconds before retry")
                TimeUnit.SECONDS.sleep(RETRY_INTERVAL_SEC)
            }
        }.execute { deleteTag(PROJECT, REPOSITORY, tag1.displayId) }
        Assertions.assertIterableEquals(listOf(tag2), getTags(PROJECT, REPOSITORY))
    }

    @Test
    fun testTagException() {
        assertThrows<IllegalArgumentException> {
            testClient.tag(vcsUrl, "", "tag")
        }
        assertThrows<IllegalArgumentException> {
            testClient.tag(vcsUrl, "left", "tag")
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
        testClient.tag(vcsUrl, tagCommitId, "tag")
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
        Assertions.assertEquals("tag", getTags(PROJECT, REPOSITORY).first().displayId)
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
        const val PROJECT = "test_project"
        const val REPOSITORY = "test-repository"
        private const val FEATURE_BRANCH = "feature"
        private const val DEVELOP_BRANCH = "develop"
    }
}
