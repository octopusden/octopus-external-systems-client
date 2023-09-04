package org.octopusden.octopus.infrastructure.common.test

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.RefNotFoundException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_BRANCH_SECTION
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.octopusden.octopus.infrastructure.common.test.dto.ChangeSet
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet
import org.slf4j.Logger
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.TimeUnit


abstract class BaseTestClient(username: String, password: String) : TestClient {

    private val repositories = mutableMapOf<String, Git>()
    private val jgitCredentialsProvider = UsernamePasswordCredentialsProvider(username, password)

    protected abstract fun getLog(): Logger
    protected abstract fun checkActive()
    protected abstract fun convertSshToHttp(vcsUrl: String): String
    protected abstract fun parseUrl(url: String): ProjectRepo
    protected abstract fun createRepository(projectRepo: ProjectRepo)
    protected abstract fun deleteRepository(projectRepo: ProjectRepo)

    override fun commit(newChangeSet: NewChangeSet, parent: String?): ChangeSet {
        waitActive()
        getLog().info("Add commit: $newChangeSet, parent: ${parent ?: ""}")
        val branch = newChangeSet.branch
        val repositoryUrl = newChangeSet.repository
        val message = newChangeSet.message

        val git = checkout(repositoryUrl, branch, parent)

        getLog().debug("Add file to repository directory, commit, push")
        val repository = git.repository

        repository
            .directory
            .toPath()
            .parent
            .resolve("${UUID.randomUUID()}.${"commit"}")
            .toFile()
            .createNewFile()
        retryableExecution {
            git.add()
                .addFilepattern(".")
                .call()
        }

        val commit = retryableExecution {
            git.commit()
                .setMessage(message)
                .call()
        }

        retryableExecution {
            git.push()
                .setCredentialsProvider(jgitCredentialsProvider)
                .call()
        }

        return ChangeSet(
            commit.id.name,
            message,
            repositoryUrl,
            branch,
            commit.authorIdent.name,
            commit.authorIdent.`when`
        )
    }


    override fun tag(vcsUrl: String, commitId: String, tag: String) {
        val git = repositories[vcsUrl]
            ?: throw IllegalArgumentException("Repository $vcsUrl does not exist, can not tag")

        gitCheckout(git, commitId)

        retryableExecution {
            git.tag()
                .setName(tag)
                .call()
        }

        retryableExecution {
            git.push()
                .setCredentialsProvider(jgitCredentialsProvider)
                .setPushTags()
                .call()
        }
    }

    override fun clearData() {
        repositories.entries
            .forEach { (vcsUrl, git) ->
                getLog().info("Clear data: $vcsUrl ")
                getLog().debug("Delete git directory: ${git.repository.directory}")
                git.repository
                    .directory
                    .deleteRecursively()

                val projectRepo = parseUrl(vcsUrl)
                getLog().debug("Delete VCS repository: '$projectRepo'")
                deleteRepository(projectRepo)
            }
        repositories.clear()
    }

    private fun getOrCreateRepo(vcsUrl: String): Git {
        return repositories.computeIfAbsent(vcsUrl) { url ->
            getLog().info("Repository $vcsUrl is not prepared, prepare")
            val projectRepo = parseUrl(url)
            createRepository(projectRepo)

            val repositoryDir = Files.createTempDirectory("TestClient_")
            getLog().debug("Clone empty repository to $repositoryDir")

            retryableExecution {
                Git.cloneRepository()
                    .setDirectory(repositoryDir.toFile())
                    .setURI(
                        convertSshToHttp(vcsUrl)
                    )
                    .setCredentialsProvider(jgitCredentialsProvider)
                    .call()
            }
        }
    }

    private fun checkout(vcsUrl: String, branch: String, parent: String?): Git {
        getLog().debug("Checkout $vcsUrl:$branch:${parent ?: ""}")
        val git = getOrCreateRepo(vcsUrl)
        val branches = git.branchList().call()

        if (branches.isEmpty()) {
            getLog().debug("Empty repository, prepare 'master'")
            parent?.let { parentValue ->
                throw IllegalArgumentException("Empty repository, parent must be unspecified, but is: '$parentValue'")
            }
            val config = git.repository.config
            config.setString(CONFIG_BRANCH_SECTION, "master", "remote", "origin")
            config.setString(CONFIG_BRANCH_SECTION, "master", "merge", "refs/heads/master")
            config.save()
            retryableExecution {
                git.commit()
                    .setMessage("initial commit")
                    .call()
            }
        }

        if (branches.any { it.name == "refs/heads/$branch" } || branch == "master") {
            getLog().debug("Branch '$branch' exists")
            parent?.let { _ ->
                throw IllegalArgumentException("Use parent only for branch creation, $branch exists")
            }
        } else {
            getLog().debug("Create branch '$branch', parent '${parent ?: "master:head"}'")
            parent?.let { parentValue ->
                gitCheckout(git, parentValue)
            }
            retryableExecution {
                git.branchCreate()
                    .setName(branch)
                    .call()
            }
        }

        getLog().debug("Checkout $branch")
        gitCheckout(git, branch)
        return git
    }

    private fun gitCheckout(git: Git, commitId: String) {
        try {
            retryableExecution {
                git.checkout()
                    .setName(commitId)
                    .call()
            }
        } catch (e: RefNotFoundException) {
            throw IllegalArgumentException("Target commit '$commitId' not found")
        }
    }

    private fun waitActive(retries: Int = 10, pingIntervalSec: Long = 3) {
        for (i in 1..retries) {
            try {
                checkActive()
                break
            } catch (ignore: Exception) {
                getLog().warn("Wait VCS is active, retries remained: ${retries - i}")
                TimeUnit.SECONDS.sleep(pingIntervalSec)
            }
        }
    }

    private fun <T> retryableExecution(attemptLimit: Int = 3, attemptIntervalSec: Long = 3, func: () -> T): T {
        lateinit var latestException: Exception
        for (attempt in 1..attemptLimit) {
            try {
                return func()
            } catch (e: TransportException) {
                getLog().warn("${e.message}, attempt=$attempt:$attemptLimit, retry in $attemptIntervalSec sec")
                latestException = e
                TimeUnit.SECONDS.sleep(attemptIntervalSec)
            }
        }
        throw latestException
    }

    protected data class ProjectRepo(val project: String, val repository: String) {
        val path: String
            get() = "$project/$repository"
    }
}
