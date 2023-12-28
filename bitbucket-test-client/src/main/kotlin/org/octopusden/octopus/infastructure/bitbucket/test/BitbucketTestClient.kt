package org.octopusden.octopus.infastructure.bitbucket.test

import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketBasicCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClassicClient
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClient
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClientParametersProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateProject
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateRepository
import org.octopusden.octopus.infrastructure.bitbucket.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.bitbucket.client.getCommits
import org.octopusden.octopus.infrastructure.bitbucket.client.getProjects
import org.octopusden.octopus.infrastructure.common.test.BaseTestClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BitbucketTestClient : BaseTestClient {
    constructor(url: String, username: String, password: String) : super(url, username, password)

    constructor(
        url: String,
        username: String,
        password: String,
        externalHost: String
    ) : super(url, username, password, externalHost)

    constructor(
        url: String,
        username: String,
        password: String,
        commitRetries: Int,
        commitPingInterval: Long,
        commitRaiseException: Boolean
    ) : super(
        url,
        username,
        password,
        commitRetries = commitRetries,
        commitPingInterval = commitPingInterval,
        commitRaiseException = commitRaiseException
    )

    constructor(
        url: String,
        username: String,
        password: String,
        externalHost: String,
        commitRetries: Int,
        commitPingInterval: Long,
        commitRaiseException: Boolean
    ) : super(url, username, password, externalHost, commitRetries, commitPingInterval, commitRaiseException)

    private val client: BitbucketClient = BitbucketClassicClient(object : BitbucketClientParametersProvider {
        override fun getApiUrl() = apiUrl
        override fun getAuth(): BitbucketCredentialProvider = BitbucketBasicCredentialProvider(username, password)
    })

    override val vcsUrlRegex = "(?:ssh://)?git@$vcsUrlHost/([^/]+)/([^/]+).git".toRegex()

    override fun Repository.getUrl() = "$apiUrl/scm/${this.path}.git"

    override fun getLog(): Logger = log

    override fun checkActive() {
        client.getProjects()
    }

    override fun createRepository(repository: Repository) {
        log.debug("[$vcsUrlHost] create repository '$repository'")
        try {
            client.getProject(repository.group)
        } catch (e: NotFoundException) {
            client.createProject(BitbucketCreateProject(repository.group, repository.group))
        }
        client.createRepository(repository.group, BitbucketCreateRepository(repository.name))
    }

    override fun deleteRepository(repository: Repository) {
        log.debug("[$vcsUrlHost] delete repository '$repository'")
        client.deleteRepository(repository.group, repository.name)
    }

    override fun checkCommit(repository: Repository, sha: String) {
        log.debug("[$vcsUrlHost] check commit '$sha' in repository '$repository'")
        client.getCommits(repository.group, repository.name, sha)
    }

    companion object {
        private val log = LoggerFactory.getLogger(BitbucketTestClient::class.java)
    }
}
