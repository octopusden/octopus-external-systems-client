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


class BitbucketTestClient(
    url: String,
    username: String,
    password: String,
    commitRetries: Int = 20,
    commitPingIntervalMsec: Long = 500,
    commitRaiseException: Boolean = true,
) : BaseTestClient(url, username, password, commitRetries, commitPingIntervalMsec, commitRaiseException) {
    private val client: BitbucketClient = BitbucketClassicClient(object : BitbucketClientParametersProvider {
        override fun getApiUrl() = url
        override fun getAuth(): BitbucketCredentialProvider = BitbucketBasicCredentialProvider(username, password)
    })

    override val urlRegex = "(?:ssh://)?git@$host[:/]([^:/]+)/([^:/]+).git".toRegex()

    override fun Repository.getHttpUrl() = "http://$host/scm/${this.path}.git"

    override fun getLog(): Logger = log

    override fun checkActive() {
        client.getProjects()
    }

    override fun createRepository(repository: Repository) {
        log.debug("[$host] create repository '$repository'")
        try {
            client.getProject(repository.group)
        } catch (e: NotFoundException) {
            client.createProject(BitbucketCreateProject(repository.group, repository.group))
        }
        client.createRepository(repository.group, BitbucketCreateRepository(repository.name))
    }

    override fun deleteRepository(repository: Repository) {
        log.debug("[$host] delete repository '$repository'")
        client.deleteRepository(repository.group, repository.name)
    }

    override fun checkCommit(repository: Repository, sha: String) {
        log.debug("[$host] check commit '$sha' in repository '$repository'")
        client.getCommits(repository.group, repository.name, sha)
    }

    companion object {
        private val log = LoggerFactory.getLogger(BitbucketTestClient::class.java)
    }
}
