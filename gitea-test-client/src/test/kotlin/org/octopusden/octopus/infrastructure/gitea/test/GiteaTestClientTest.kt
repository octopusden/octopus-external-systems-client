package org.octopusden.octopus.infrastructure.gitea.test

import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.CredentialProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.common.test.BaseTestClientTest
import org.octopusden.octopus.infrastructure.gitea.client.GiteaClassicClient
import org.octopusden.octopus.infrastructure.gitea.client.createPullRequestWithDefaultReviewers
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCommit
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaPullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaTag
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

    override fun getTags(project: String, repository: String): Collection<TestTag> =
        client.getTags(project, repository).map { t -> t.toTestTag() }

    override fun getCommits(project: String, repository: String, branch: String): Collection<TestCommit> =
        client.getCommits(project, repository, null, branch).map { c -> c.toTestCommit() }

    override fun createPullRequestWithDefaultReviewers(
        project: String,
        repository: String,
        sourceBranch: String,
        targetBranch: String,
        title: String,
        description: String
    ): TestPullRequest =
        client.createPullRequestWithDefaultReviewers(
            project,
            repository,
            sourceBranch,
            targetBranch,
            title,
            description
        ).toTestPullRequest()

    private fun GiteaTag.toTestTag() = TestTag(name, commit.sha)
    private fun GiteaCommit.toTestCommit() = TestCommit(sha, commit.message)
    private fun GiteaPullRequest.toTestPullRequest() = TestPullRequest(id)
}
