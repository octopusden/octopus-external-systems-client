package org.octopusden.octopus.infastructure.bitbucket.test

import org.octopusden.octopus.infastructure.bitbucket.test.dto.ChangeSet
import org.octopusden.octopus.infastructure.bitbucket.test.dto.NewChangeSet
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.RefNotFoundException
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_BRANCH_SECTION
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketBasicCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClassicClient
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClient
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketClientParametersProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.BitbucketCredentialProvider
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateProject
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateRepository
import org.octopusden.octopus.infrastructure.bitbucket.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.bitbucket.client.getProjects
import org.slf4j.LoggerFactory
import java.net.URI
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.TimeUnit


class BitbucketTestClient(
    val bitbucketHost: String,
    username: String,
    password: String
) {
    private val repositories = mutableMapOf<String, Git>()
    private val jgitCredentialsProvider = UsernamePasswordCredentialsProvider(username, password)

    private val client: BitbucketClient = BitbucketClassicClient(object : BitbucketClientParametersProvider {
        override fun getApiUrl() = "http://$bitbucketHost"
        override fun getAuth(): BitbucketCredentialProvider = BitbucketBasicCredentialProvider(username, password)
    })

    fun commit(newChangeSet: NewChangeSet, parent: String? = null): ChangeSet {
        waitActive()
        log.info("Add commit: $newChangeSet, parent: ${parent ?: ""}")
        val branch = newChangeSet.branch
        val repositoryUrl = newChangeSet.repository
        val message = newChangeSet.message

        val git = checkout(repositoryUrl, branch, parent)

        log.debug("Add file to repository directory, commit, push")
        val repository = git.repository

        repository
            .directory
            .toPath()
            .parent
            .resolve("${UUID.randomUUID()}.${"commit"}")
            .toFile()
            .createNewFile()
        git.add()
            .addFilepattern(".")
            .call()

        val commit = git.commit()
            .setMessage(message)
            .call()

        git.push()
            .setCredentialsProvider(jgitCredentialsProvider)
            .call()

        return ChangeSet(
            commit.id.name,
            message,
            repositoryUrl,
            branch,
            commit.authorIdent.name,
            commit.authorIdent.`when`
        )
    }

    fun tag(vcsUrl: String, commitId: String, tag: String) {
        val git = repositories[vcsUrl]
            ?: throw IllegalArgumentException("Repository $vcsUrl does not exist, can not tag")

        gitCheckout(git, commitId)

        git.tag()
            .setName(tag)
            .call()

        git.push()
            .setCredentialsProvider(jgitCredentialsProvider)
            .setPushTags()
            .call()
    }

    fun clearData() {
        repositories.entries
            .forEach { (vcsUrl, git) ->
                log.info("Clear data: $vcsUrl ")
                log.debug("Delete git directory: ${git.repository.directory}")
                git.repository
                    .directory
                    .deleteRecursively()

                val (project, repo) = vcsUrl.parseBitbucketUrl()
                log.debug("Delete Bitbucket repository: '$project:$repo'")
                client.deleteRepository(project, repo)
            }
        repositories.clear()
    }

    private fun waitActive(retries: Int = 10, pingIntervalSec: Long = 3) {
        for (i in 1..retries) {
            try {
                client.getProjects()
                break
            } catch (ignore: Exception) {
                log.warn("Wait Bitbucket is active, retries remained: ${retries - i}")
                TimeUnit.SECONDS.sleep(pingIntervalSec)
            }
        }
    }

    private fun checkout(vcsUrl: String, branch: String, parent: String?): Git {
        log.debug("Checkout $vcsUrl:$branch:${parent ?: ""}")
        val git = getOrCreateRepo(vcsUrl)
        val branches = git.branchList().call()

        if (branches.isEmpty()) {
            log.debug("Empty repository, prepare 'master'")
            parent?.let { parentValue ->
                throw IllegalArgumentException("Empty repository, parent must be unspecified, but is: '$parentValue'")
            }
            val config = git.repository.config
            config.setString(CONFIG_BRANCH_SECTION, "master", "remote", "origin")
            config.setString(CONFIG_BRANCH_SECTION, "master", "merge", "refs/heads/master")
            config.save()
            git.commit()
                .setMessage("initial commit")
                .call()
        }

        if (branches.any { it.name == "refs/heads/$branch" } || branch == "master") {
            log.debug("Branch '$branch' exists")
            parent?.let { _ ->
                throw IllegalArgumentException("Use parent only for branch creation, $branch exists")
            }
        } else {
            log.debug("Create branch '$branch', parent '${parent ?: "master:head"}'")
            parent?.let { parentValue ->
                gitCheckout(git, parentValue)
            }
            git.branchCreate()
                .setName(branch)
                .call()
        }

        log.debug("Checkout $branch")
        gitCheckout(git, branch)
        return git
    }

    private fun gitCheckout(git: Git, commitId: String) {
        try {
            git.checkout()
                .setName(commitId)
                .call()
        } catch (e: RefNotFoundException) {
            throw IllegalArgumentException("Target commit '$commitId' not found")
        }
    }

    private fun getOrCreateRepo(vcsUrl: String): Git {
        return repositories.computeIfAbsent(vcsUrl) { url ->
            log.info("Repository $vcsUrl is not prepared, prepare")
            val projectRepo = url.parseBitbucketUrl()
            val project = projectRepo.first
            val repository = projectRepo.second
            createProjectIfNotExist(project)
            log.debug("Create Bitbucket Repository: $project:$repository")

            client.createRepository(project, BitbucketCreateRepository(repository))

            val repositoryDir = Files.createTempDirectory("BitbucketTestClient_")
            log.debug("Clone empty repository to $repositoryDir")

            val uri = URI.create(vcsUrl)
            Git.cloneRepository()
                .setDirectory(repositoryDir.toFile())
                .setURI(
                    "http://$bitbucketHost/scm${uri.path}"
                )
                .setCredentialsProvider(jgitCredentialsProvider)
                .call()
        }
    }

    private fun createProjectIfNotExist(project: String) {
        try {
            client.getProject(project)
        } catch (e: NotFoundException) {
            client.createProject(BitbucketCreateProject(project, project))
        }
    }

    private fun String.parseBitbucketUrl(): ProjectRepo {
        val uri = URI.create(this)
        val path = uri.path
        val projectRepoArray = path.substring(1, path.indexOf(".git"))
            .split("/")
        if (projectRepoArray.size != 2) {
            throw IllegalArgumentException("Repository '$this' is not a Bitbucket repo, current host: '$bitbucketHost'")
        }
        return projectRepoArray[0] to projectRepoArray[1]
    }

    companion object {
        private val log = LoggerFactory.getLogger(BitbucketTestClient::class.java)
    }
}

typealias ProjectRepo = Pair<String, String>
