package org.octopusden.octopus.infrastructure.gitlab.test

import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.GitLabApiException
import org.gitlab4j.api.models.Group
import org.gitlab4j.api.models.GroupParams
import org.octopusden.octopus.infrastructure.common.test.BaseTestClient
import org.slf4j.LoggerFactory

class GitlabTestClient(val url: String, username: String, password: String) : BaseTestClient(username, password) {
    private val client = GitLabApi.oauth2Login(url, username, password)
    override fun getLog() = log

    override fun checkActive() {
        client.version
    }

    override fun convertSshToHttp(vcsUrl: String): String = "$url/${vcsUrl.substring(vcsUrl.lastIndexOf(":") + 1)}"

    override fun parseUrl(url: String): ProjectRepo {
        val projectRepoArray = url.substring(url.lastIndexOf(":") + 1, url.indexOf(".git"))
            .split("/")
        if (projectRepoArray.size != 2) {
            throw IllegalArgumentException("Repository '$url' is not a Gitlab repo, current url: '${this.url}'")
        }
        return ProjectRepo(projectRepoArray[0], projectRepoArray[1])
    }

    private fun createProjectIfNotExist(project: String): Group {
        val groupParams = GroupParams()
        groupParams.withPath(project)
        groupParams.withName(project)
        return client.groupApi.getGroups(project).firstOrNull { g -> g.path == project }
            ?: client.groupApi.createGroup(groupParams)
    }

    override fun createRepository(projectRepo: ProjectRepo) {
        val group = createProjectIfNotExist(projectRepo.project)
        try {
            client.projectApi.createProject(group.id, projectRepo.repository)
        } catch (e: GitLabApiException) {
            throw IllegalArgumentException(e.message, e)
        }
    }

    override fun deleteRepository(projectRepo: ProjectRepo) {
        client.projectApi.deleteProject("${projectRepo.project}/${projectRepo.repository}")
    }

    companion object {
        private val log = LoggerFactory.getLogger(GitlabTestClient::class.java)
    }
}
