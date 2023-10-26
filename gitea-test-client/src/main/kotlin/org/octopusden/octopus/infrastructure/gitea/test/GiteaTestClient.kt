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

class GiteaTestClient(val url: String, val username: String, val password: String) :
    BaseTestClient(username, password) {

    private val client = GiteaClassicClient(object : ClientParametersProvider {
        override fun getApiUrl(): String = url
        override fun getAuth(): CredentialProvider = StandardBasicCredCredentialProvider(username, password)
    })

    override fun getLog(): Logger = log

    override fun checkActive() {
        client.getOrganizations()
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

    override fun createRepository(projectRepo: ProjectRepo) {
        createOrganizationIfNotExist(projectRepo.project)
        client.createRepository(projectRepo.project, GiteaCreateRepository(projectRepo.repository))
    }

    override fun deleteRepository(projectRepo: ProjectRepo) =
        client.deleteRepository(projectRepo.project, projectRepo.repository)

    override fun checkCommit(projectRepo: ProjectRepo, sha: String) {
        client.getCommits(projectRepo.project, projectRepo.repository, sha)
    }

    private fun createOrganizationIfNotExist(organization: String) {
        try {
            client.getOrganization(organization)
        } catch (e: NotFoundException) {
            client.createOrganization(GiteaCreateOrganization(organization))
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(GiteaTestClient::class.java)
    }
}
