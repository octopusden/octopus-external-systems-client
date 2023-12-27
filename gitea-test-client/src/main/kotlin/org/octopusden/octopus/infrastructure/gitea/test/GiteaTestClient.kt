package org.octopusden.octopus.infrastructure.gitea.test

import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.CredentialProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.common.test.BaseTestClient
import org.octopusden.octopus.infrastructure.gitea.client.GiteaClassicClient
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateRepository
import org.octopusden.octopus.infrastructure.gitea.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.gitea.client.getCommits
import org.octopusden.octopus.infrastructure.gitea.client.getOrganizations
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class GiteaTestClient(
    url: String,
    username: String,
    password: String,
    host: String? = null,
    commitRetries: Int = 20,
    commitPingInterval: Long = 500,
    commitRaiseException: Boolean = true,
) : BaseTestClient(url, username, password, host, commitRetries, commitPingInterval, commitRaiseException) {
    private val client = GiteaClassicClient(object : ClientParametersProvider {
        override fun getApiUrl(): String = apiUrl
        override fun getAuth(): CredentialProvider = StandardBasicCredCredentialProvider(username, password)
    })

    override val vcsUrlRegex = "(?:ssh://)?git@$vcsUrlHost[:/]([^:/]+)/([^:/]+).git".toRegex()

    override fun Repository.getUrl() = "$apiUrl/${this.path}.git"

    override fun getLog(): Logger = log

    override fun checkActive() {
        client.getOrganizations()
    }

    override fun createRepository(repository: Repository) {
        log.debug("[$vcsUrlHost] create repository '$repository'")
        try {
            client.getOrganization(repository.group)
        } catch (e: NotFoundException) {
            client.createOrganization(GiteaCreateOrganization(repository.group))
        }
        client.createRepository(repository.group, GiteaCreateRepository(repository.name))
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
        private val log = LoggerFactory.getLogger(GiteaTestClient::class.java)
    }
}
