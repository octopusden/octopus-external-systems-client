package org.octopusden.octopus.infastructure.bitbucket.test

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketBasicCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClassicClient
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClientParametersProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.createPullRequestWithDefaultReviewers
import org.octopusden.octopus.infrastructure.bitbucket.client.getCommits
import org.octopusden.octopus.infrastructure.bitbucket.client.getTags
import org.slf4j.LoggerFactory

const val HOST = "localhost:7990"
const val USER = "admin"
const val PASSWORD = "admin"

const val PROJECT = "test-project"
const val REPOSITORY = "test-repository"

const val VCS_URL = "ssh://git@$HOST/$PROJECT/$REPOSITORY.git"

const val TAG = "test_tag"

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BitbucketTestClientTest {
    private val client = BitbucketClassicClient(object : BitbucketClientParametersProvider {
        override fun getApiUrl() = "http://$HOST"
        override fun getAuth(): BitbucketCredentialProvider = BitbucketBasicCredentialProvider(USER, PASSWORD)
    })

    private val testClient =
        BitbucketTestClient(HOST, USER, PASSWORD)

    @BeforeEach
    fun beforeEachBitbucketTestClientTest() {
        testClient.clearData()
    }

    @Test
    fun testCommit() {
        testClient.commit(
            NewChangeSet(
                "master commit 1",
                VCS_URL,
                "master"
            )
        )
        testClient.commit(
            NewChangeSet(
                "master commit 2",
                VCS_URL,
                "master"
            )
        )
        val firstDevelopCommitId = testClient.commit(
            NewChangeSet(
                "develop commit 1",
                VCS_URL,
                "develop"
            )
        )
        testClient.commit(
            NewChangeSet(
                "develop commit 2",
                VCS_URL,
                "develop"
            )
        )
        testClient.commit(
            NewChangeSet(
                "feature commit 1",
                VCS_URL,
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
            testClient.commit(NewChangeSet("message", VCS_URL, "develop"), "leftId")
        }

        val commitId = testClient.commit(NewChangeSet("message", VCS_URL, "develop")).id
        assertThrows<IllegalArgumentException> {
            testClient.commit(NewChangeSet("message", VCS_URL, "develop"), commitId)
        }
    }

    @Test
    fun testTag() {
        val expectedId = testClient.commit(NewChangeSet("test tag commit", VCS_URL, "master")).id
        testClient.tag(VCS_URL, expectedId, TAG)
        Assertions.assertEquals(TAG, client.getTags(PROJECT, REPOSITORY).first().displayId)
        Assertions.assertEquals(expectedId, client.getTags(PROJECT, REPOSITORY).first().latestCommit)
    }

    @Test
    fun testTagException() {
        assertThrows<IllegalArgumentException> {
            testClient.tag(VCS_URL, "", TAG)
        }
        testClient.commit(NewChangeSet("message", VCS_URL, "develop")).id
        assertThrows<IllegalArgumentException> {
            testClient.tag(VCS_URL, "left", TAG)
        }
    }

    @Test
    fun testPullRequest() {
        val mainBranch = "master"
        testClient.commit(NewChangeSet("initial commit", VCS_URL, mainBranch))
        val featureBranch = "feature"
        testClient.commit(NewChangeSet("feature commit", VCS_URL, featureBranch))
        val pullRequest = client.createPullRequestWithDefaultReviewers(
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
            client.getCommits(PROJECT, REPOSITORY, null, null, branch).map { it.message }
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(BitbucketTestClientTest::class.java)
    }
}
