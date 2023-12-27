package org.octopusden.octopus.infrastructure.gitlab.test

import java.util.Date
import java.util.concurrent.TimeUnit
import org.gitlab4j.api.GitLabApi
import org.gitlab4j.api.GitLabApiException
import org.gitlab4j.api.models.GroupParams
import org.octopusden.octopus.infrastructure.common.test.BaseTestClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GitlabTestClient(
    url: String,
    username: String,
    password: String,
    vcsUrlHost: String? = null,
    commitRetries: Int = 20,
    commitPingInterval: Long = 500,
    commitRaiseException: Boolean = true,
) : BaseTestClient(url, username, password, vcsUrlHost, commitRetries, commitPingInterval, commitRaiseException) {
    private val client = GitLabApi.oauth2Login(apiUrl, username, password)

    override val vcsUrlRegex = "(?:ssh://)?git@${this.vcsUrlHost}:((?:[^/]+/)+)([^/]+).git".toRegex()

    override fun Repository.getUrl() = "$apiUrl/${this.path}.git"

    override fun getLog(): Logger = log

    override fun checkActive() {
        client.version
    }

    override fun createRepository(repository: Repository) {
        log.debug("[$vcsUrlHost] create repository '$repository'")
        if (existRepository(repository)) {
            log.error("[$vcsUrlHost] repository '$repository' exists already (previous test(s) probably crashed)")
            deleteRepository(repository)
        }
        val groupParams = GroupParams()
        groupParams.withPath(repository.group)
        groupParams.withName(repository.group)
        val group = retryableExecution {
            client.groupApi.getGroups(repository.group).firstOrNull { g -> g.path == repository.group }
                ?: client.groupApi.createGroup(groupParams)
        }
        retryableExecution {
            client.projectApi.createProject(group.id, repository.name)
        }
    }

    override fun deleteRepository(repository: Repository) {
        log.debug("[$vcsUrlHost] delete repository '$repository'")
        if (existRepository(repository)) {
            try {
                val project = retryableExecution { client.projectApi.getProject(repository.path) }
                val time = Date().time
                project.name = "${project.name}-$time"
                project.path = "${project.path}-$time"
                project.withDefaultBranch(null)
                retryableExecution { client.projectApi.updateProject(project) }
                while (existRepository(repository)) {
                    log.warn("[$vcsUrlHost] wait till repository '$repository' is moved for deleted")
                    TimeUnit.SECONDS.sleep(1)
                }
                retryableExecution { client.projectApi.deleteProject(project.id) }
            } catch (e: GitLabApiException) {
                log.error(e.message)
            }
        }
    }

    override fun checkCommit(repository: Repository, sha: String) {
        log.debug("[$vcsUrlHost] check commit '$sha' in repository '$repository'")
        client.commitsApi.getCommits(repository.path, sha, null, null)
    }

    private fun existRepository(repository: Repository): Boolean {
        return try {
            retryableExecution {
                client.projectApi.getProject(repository.path) != null
            }
        } catch (e: GitLabApiException) {
            false
        }
    }

    private fun <T> retryableExecution(
        attemptLimit: Int = 3, attemptIntervalSec: Long = 3, skipRetryOnCode: Int = 404, func: () -> T
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
