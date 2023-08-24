package org.octopusden.octopus.infrastructure.gitlab.test

import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.models.Commit
import org.gitlab4j.api.models.MergeRequest
import org.gitlab4j.api.models.Tag
import org.octopusden.octopus.infrastructure.common.test.BaseTestClientTest

private const val HOST = "localhost:8990"
private const val USER = "root"
private const val PASSWORD = "VomkaEa6PD1OIgY7dQVbPUuO8wi9RMCaZw/i9yPXcI0="

class GitlabtTestClientTest : BaseTestClientTest(
    GitlabTestClient("http://$HOST", USER, PASSWORD), "ssh://git@$HOST:%s/%s.git", "test_tag"
) {

    private val client = GitLabApi.oauth2Login("http://$HOST", USER, PASSWORD)

    override fun getTags(project: String, repository: String): Collection<TestTag> {
        val prj = client.projectApi.getProject(project, repository)
        return client.tagsApi.getTags(prj.id).map { t -> t.toTestTag() }
    }

    override fun getCommits(project: String, repository: String, branch: String): List<TestCommit> {
        val prj = client.projectApi.getProject(project, repository)
        return client.commitsApi.getCommits(prj.id, branch, null, null).map { c -> c.toTestCommit() }
    }

    override fun createPullRequestWithDefaultReviewers(
        project: String,
        repository: String,
        sourceBranch: String,
        targetBranch: String,
        title: String,
        description: String
    ): TestPullRequest {
        val prj = client.projectApi.getProject(project, repository)

        return client.mergeRequestApi.createMergeRequest(
            prj.id,
            sourceBranch,
            targetBranch,
            title,
            description,
            0L
        ).toTestPullRequest()
    }

    private fun Tag.toTestTag() = TestTag(name, commit.id)
    private fun Commit.toTestCommit() = TestCommit(id, message)
    private fun MergeRequest.toTestPullRequest() = TestPullRequest(id)
}
