package org.octopusden.octopus.infastructure.bitbucket.test

import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketBasicCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClassicClient
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClientParametersProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.createPullRequestWithDefaultReviewers
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCommit
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketPullRequest
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketTag
import org.octopusden.octopus.infrastructure.bitbucket.client.getCommits
import org.octopusden.octopus.infrastructure.bitbucket.client.getTags
import org.octopusden.octopus.infrastructure.common.test.BaseTestClientTest

const val HOST = "localhost:7990"
const val USER = "admin"
const val PASSWORD = "admin"

const val PROJECT = "test-project"
const val REPOSITORY = "test-repository"

const val VCS_URL = "ssh://git@$HOST/$PROJECT/$REPOSITORY.git"

const val TAG = "test_tag"

class BitbucketTestClientTest : BaseTestClientTest(
    BitbucketTestClient(HOST, USER, PASSWORD), VCS_URL, PROJECT, REPOSITORY, TAG
) {

    private val client = BitbucketClassicClient(object : BitbucketClientParametersProvider {
        override fun getApiUrl() = "http://$HOST"
        override fun getAuth(): BitbucketCredentialProvider = BitbucketBasicCredentialProvider(USER, PASSWORD)
    })

    override fun getTags(project: String, repository: String): Collection<TestTag> =
        client.getTags(project, repository).map { t -> t.toTestTag() }

    override fun getCommits(project: String, repository: String, branch: String) =
        client.getCommits(project, repository, null, null, branch).map { c -> c.toTestCommit() }

    override fun createPullRequestWithDefaultReviewers(
        project: String,
        repository: String,
        sourceBranch: String,
        targetBranch: String,
        title: String,
        description: String
    ): TestPullRequest = client.createPullRequestWithDefaultReviewers(
        project,
        repository,
        sourceBranch,
        targetBranch,
        title,
        description
    ).toTestPullRequest()

    private fun BitbucketTag.toTestTag() = TestTag(displayId, latestCommit)
    private fun BitbucketCommit.toTestCommit() = TestCommit(id, message)
    private fun BitbucketPullRequest.toTestPullRequest() = TestPullRequest(id)
}
