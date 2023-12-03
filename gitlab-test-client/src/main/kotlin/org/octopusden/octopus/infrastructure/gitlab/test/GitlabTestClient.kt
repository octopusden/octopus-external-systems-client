package org.octopusden.octopus.infrastructure.gitlab.test

import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.GitLabApiException
import org.gitlab4j.api.models.Group
import org.gitlab4j.api.models.GroupParams
import org.octopusden.octopus.infrastructure.common.test.BaseTestClient
import org.slf4j.LoggerFactory
import java.util.Date
import java.util.concurrent.TimeUnit

class GitlabTestClient(
    val url: String,
    username: String,
    password: String,
    commitRetries: Int = 20,
    commitPingIntervalMsec: Long = 500,
    commitRaiseException: Boolean = true,
) : BaseTestClient(
    username,
    password,
    commitRetries,
    commitPingIntervalMsec,
    commitRaiseException,
) {
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
        return retryableExecution {
            client.groupApi.getGroups(project).firstOrNull { g -> g.path == project }
                ?: client.groupApi.createGroup(groupParams)
        }
    }

    override fun createRepository(projectRepo: ProjectRepo) {
        if (existRepository(projectRepo)) {
            log.error("Repository ${projectRepo.path} exists (previous test(s) probably crashed)")
            deleteRepository(projectRepo)
        }

        val group = createProjectIfNotExist(projectRepo.project)
        retryableExecution {
            client.projectApi.createProject(group.id, projectRepo.repository)
        }
    }

    override fun deleteRepository(projectRepo: ProjectRepo) {
        log.info("Delete ${projectRepo.path}")
        if (existRepository(projectRepo)) {
            try {
                val projectId = moveProjectForDelete(projectRepo)
                retryableExecution { client.projectApi.deleteProject(projectId) }
            } catch (e: GitLabApiException) {
                log.error(e.message)
            }
        }
    }

    override fun checkCommit(projectRepo: ProjectRepo, sha: String) {
        client.commitsApi.getCommits(projectRepo.path, sha, null, null)
    }

    private fun moveProjectForDelete(projectRepo: ProjectRepo): Long {
        val project = retryableExecution { client.projectApi.getProject(projectRepo.path) }

        val time = Date().time
        project.name = "${project.name}-$time"
        project.path = "${project.path}-$time"
        project.withDefaultBranch(null)

        retryableExecution { client.projectApi.updateProject(project) }

        while (existRepository(projectRepo)) {
            log.warn("Wait when ${projectRepo.path} will be moved for delete")
            TimeUnit.SECONDS.sleep(1)
        }

        return project.id
    }

    private fun existRepository(projectRepo: ProjectRepo): Boolean {
        return try {
            retryableExecution {
                client.projectApi.getProject(projectRepo.path) != null
            }
        } catch (e: GitLabApiException) {
            false
        }
    }

    private fun <T> retryableExecution(
        attemptLimit: Int = 3,
        attemptIntervalSec: Long = 3,
        skipRetryOnCode: Int = 404,
        func: () -> T
    ): T {
        lateinit var latestException: Exception
        for (attempt in 1..attemptLimit) {
            try {
                return func()
            } catch (e: GitLabApiException) {
                if (e.httpStatus == skipRetryOnCode) {
                    throw e
                }
                getLog().warn("${e.message}, attempt=$attempt:$attemptLimit, retry in $attemptIntervalSec sec")
                latestException = e
                TimeUnit.SECONDS.sleep(attemptIntervalSec)
            }
        }
        throw latestException
    }

    companion object {
        private val log = LoggerFactory.getLogger(GitlabTestClient::class.java)
    }
}
