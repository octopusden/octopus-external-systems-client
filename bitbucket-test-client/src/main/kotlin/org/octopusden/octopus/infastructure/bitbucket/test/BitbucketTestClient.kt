package org.octopusden.octopus.infastructure.bitbucket.test

import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketBasicCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClassicClient
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClient
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClientParametersProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateProject
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateRepository
import org.octopusden.octopus.infrastructure.bitbucket.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.bitbucket.client.getProjects
import org.octopusden.octopus.infrastructure.common.test.BaseTestClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI


class BitbucketTestClient(
    val bitbucketHost: String,
    username: String,
    password: String
) : BaseTestClient(username, password) {

    private val client: BitbucketClient = BitbucketClassicClient(object : BitbucketClientParametersProvider {
        override fun getApiUrl() = "http://$bitbucketHost"
        override fun getAuth(): BitbucketCredentialProvider = BitbucketBasicCredentialProvider(username, password)
    })

    override fun getLog(): Logger = log

    override fun checkActive() {
        client.getProjects()
    }

    override fun convertSshToHttp(vcsUrl: String): String {
        val uri = URI.create(vcsUrl)
        return "http://$bitbucketHost/scm${uri.path}"
    }

    override fun parseUrl(url: String): ProjectRepo {
        val uri = URI.create(url)
        val path = uri.path
        val projectRepoArray = path.substring(1, path.indexOf(".git"))
            .split("/")
        if (projectRepoArray.size != 2) {
            throw IllegalArgumentException("Repository '$url' is not a Bitbucket repo, current host: '$bitbucketHost'")
        }
        return ProjectRepo(projectRepoArray[0], projectRepoArray[1])
    }

    private fun createProjectIfNotExist(project: String) {
        try {
            client.getProject(project)
        } catch (e: NotFoundException) {
            client.createProject(BitbucketCreateProject(project, project))
        }
    }

    override fun createRepository(projectRepo: ProjectRepo) {
        createProjectIfNotExist(projectRepo.project)
        client.createRepository(projectRepo.project, BitbucketCreateRepository(projectRepo.repository))
    }

    override fun deleteRepository(projectRepo: ProjectRepo) {
        client.deleteRepository(projectRepo.project, projectRepo.repository)
    }

    companion object {
        private val log = LoggerFactory.getLogger(BitbucketTestClient::class.java)
    }
}
