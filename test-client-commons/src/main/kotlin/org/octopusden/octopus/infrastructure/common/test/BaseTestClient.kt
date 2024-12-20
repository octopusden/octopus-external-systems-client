package org.octopusden.octopus.infrastructure.common.test

import java.io.File
import java.nio.file.Files
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.RefNotFoundException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_BRANCH_SECTION
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.octopusden.octopus.infrastructure.common.test.dto.ChangeSet
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet
import org.slf4j.Logger

abstract class BaseTestClient(
    url: String,
    protected val username: String,
    protected val password: String,
    externalHost: String? = null,
    private val commitRetries: Int = 20,
    private val commitPingInterval: Long = 500,
    private val commitRaiseException: Boolean = true
) : TestClient {
    private val repositories = mutableMapOf<Repository, Git>()
    private val jgitCredentialsProvider = UsernamePasswordCredentialsProvider(username, password)

    protected val apiUrl = url.trimEnd('/')
    protected val vcsUrlHost = externalHost?.lowercase() ?: apiUrl.lowercase().replace("^(https|http)://".toRegex(), "")
    protected abstract val vcsUrlRegex: Regex

    private fun parseUrl(vcsUrl: String) = vcsUrl.lowercase().let { loweredVcsUrl ->
        vcsUrlRegex.find(loweredVcsUrl)?.let { result ->
            result.destructured.let { Repository(it.component1().trimEnd('/'), it.component2(), loweredVcsUrl) }
        } ?: throw IllegalArgumentException("VCS URL '$vcsUrl' is not supported by ${javaClass.simpleName}($vcsUrlHost)")
    }

    protected abstract fun Repository.getUrl(): String
    protected abstract fun getLog(): Logger
    protected abstract fun checkActive()
    protected abstract fun createRepository(repository: Repository)
    protected abstract fun deleteRepository(repository: Repository)
    protected abstract fun checkCommit(repository: Repository, sha: String)

    override fun commit(newChangeSet: NewChangeSet, parent: String?): ChangeSet {
        val repository = parseUrl(newChangeSet.repository)
        getLog().info(
            "[$vcsUrlHost] commit into repository '$repository' branch '${newChangeSet.branch}'" + if (parent != null) " (parent '$parent')" else ""
        )
        val git = checkout(repository, newChangeSet.branch, parent)
        git.repository.directory.toPath().parent.resolve("${UUID.randomUUID()}.${"commit"}").toFile().createNewFile()
        retryableExecution {
            git.add().addFilepattern(".").call()
        }
        val commit = retryableExecution {
            git.commit().setMessage(newChangeSet.message).call()
        }
        retryableExecution {
            git.push().setCredentialsProvider(jgitCredentialsProvider).call()
        }
        wait(
            waitMessage = "[$vcsUrlHost] wait commit '${commit.id.name}' is accessible in repository '$repository'",
            pingInterval = commitPingInterval,
            retries = commitRetries,
            raiseOnException = commitRaiseException,
            failMessage = "[$vcsUrlHost] commit '${commit.id.name}' is not reflected in repository '$repository' within the %d seconds"
        ) {
            checkCommit(repository, commit.id.name)
        }
        return ChangeSet(
            commit.id.name,
            commit.fullMessage,
            newChangeSet.repository,
            newChangeSet.branch,
            commit.authorIdent.name,
            commit.authorIdent.`when`
        )
    }


    override fun tag(vcsUrl: String, commitId: String, tag: String) {
        val repository = parseUrl(vcsUrl)
        getLog().info("[$vcsUrlHost] tag commit '$commitId' in '$repository' as '$tag'")
        val git = repositories[repository]
            ?: throw IllegalArgumentException("[$vcsUrlHost] repository '$repository' does not exist, can not tag")
        gitCheckout(git, commitId)
        retryableExecution {
            git.tag().setName(tag).call()
        }
        retryableExecution {
            git.push().setCredentialsProvider(jgitCredentialsProvider).setPushTags().call()
        }
    }

    override fun exportRepository(vcsUrl: String, zip: File) {
        val repository = parseUrl(vcsUrl)
        getLog().info("[$vcsUrlHost] export repository '$repository'")
        val git = repositories[repository]
            ?: throw IllegalArgumentException("[$vcsUrlHost] repository '$repository' does not exist, can not export")
        val repositoryDir = git.repository.directory
        ZipOutputStream(zip.outputStream()).use { zipOutputStream ->
            repositoryDir.walkTopDown().forEach { file ->
                if (file != repositoryDir) {
                    zipOutputStream.putNextEntry(
                        ZipEntry(
                            repositoryDir.toPath().relativize(file.toPath())
                                .toString() + if (file.isDirectory) "/" else ""
                        )
                    )
                    if (file.isFile) {
                        file.inputStream().use { it.copyTo(zipOutputStream) }
                    }
                    zipOutputStream.closeEntry()
                }
            }
        }
    }

    override fun importRepository(vcsUrl: String, zip: File) {
        val repository = parseUrl(vcsUrl)
        getLog().info("[$vcsUrlHost] import repository '$repository'")
        if (repositories.contains(repository)) {
            throw IllegalArgumentException("[$vcsUrlHost] repository '$repository' exists already, can not import")
        }
        createRepository(repository)
        val repositoryDir = Files.createTempDirectory("TestClient_")
        val gitDir = repositoryDir.resolve(".git").also { Files.createDirectory(it) }
        ZipInputStream(zip.inputStream()).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (entry.isDirectory) {
                    Files.createDirectory(gitDir.resolve(entry.name.trimEnd('/')))
                } else {
                    gitDir.resolve(entry.name).toFile().outputStream().use {
                        zipInputStream.copyTo(it)
                    }
                }
                entry = zipInputStream.nextEntry
            }
        }
        val git = Git.open(repositoryDir.toFile())
        git.remoteList().call().forEach {
            git.remoteRemove().setRemoteName(it.name).call()
        }
        git.remoteAdd().setName("origin").setUri(URIish(repository.getUrl())).call()
        retryableExecution {
            git.push().setCredentialsProvider(jgitCredentialsProvider).setPushAll().setPushTags().call()
        }
        val commitId = git.log().call().first().id.name
        wait(
            waitMessage = "[$vcsUrlHost] wait commit '$commitId' is accessible in repository '$repository'",
            pingInterval = commitPingInterval,
            retries = commitRetries,
            raiseOnException = commitRaiseException,
            failMessage = "[$vcsUrlHost] commit '$commitId' is not reflected in repository '$repository' within the %d seconds"
        ) {
            checkCommit(repository, commitId)
        }
        repositories[repository] = git
    }

    override fun getCommits(vcsUrl: String, branch: String): List<ChangeSet> {
        val repository = parseUrl(vcsUrl)
        getLog().info(
            "[$vcsUrlHost] get commits from repository '$repository' (branch '$branch')"
        )
        val git = repositories[repository]
            ?: throw IllegalArgumentException("[$vcsUrlHost] repository '$repository' does not exist, can not get commits")
        return git.log().add(git.repository.resolve(branch)).call().map {
            ChangeSet(it.id.name, it.fullMessage, vcsUrl, branch, it.authorIdent.name, it.authorIdent.`when`)
        }
    }

    override fun clearData(): Collection<String> {
        getLog().info("[$vcsUrlHost] clear all data")
        repositories.entries.forEach { (repository, git) ->
            getLog().debug("[{}] delete directory '{}'", vcsUrlHost, git.repository.directory)
            git.repository.directory.deleteRecursively()
            deleteRepository(repository)
        }
        val repositoryUrls = repositories.keys.map { repository -> repository.sshUrl }
        repositories.clear()
        return repositoryUrls
    }

    private fun checkout(repository: Repository, branch: String, parent: String?): Git {
        getLog().debug(
            "[{}] checkout repository '{}' branch '{}'{}",
            vcsUrlHost,
            repository,
            branch,
            if (parent != null) " (parent '$branch')" else ""
        )
        val git = repositories.computeIfAbsent(repository) { _ ->
            createRepository(repository)
            val repositoryDir = Files.createTempDirectory("TestClient_")
            getLog().debug("[{}] clone repository '{}' to directory '{}'", vcsUrlHost, repository, repositoryDir)
            retryableExecution {
                Git.cloneRepository().setDirectory(repositoryDir.toFile()).setURI(repository.getUrl())
                    .setCredentialsProvider(jgitCredentialsProvider).call()
            }
        }
        val branches = git.branchList().call()
        if (branches.isEmpty()) {
            getLog().debug(
                "[{}] repository '{}' is empty, prepare default branch '{}'",
                vcsUrlHost,
                repository,
                DEFAULT_BRANCH
            )
            parent?.let {
                throw IllegalArgumentException("[$vcsUrlHost] repository '$repository' is empty, but parent '$it' is specified")
            }
            val config = git.repository.config
            config.setString(CONFIG_BRANCH_SECTION, DEFAULT_BRANCH, "remote", "origin")
            config.setString(CONFIG_BRANCH_SECTION, DEFAULT_BRANCH, "merge", "refs/heads/$DEFAULT_BRANCH")
            config.save()
            retryableExecution {
                git.commit().setMessage(INITIAL_COMMIT_MESSAGE).call()
            }
        }
        if (branches.any { it.name == "refs/heads/$branch" } || branch == DEFAULT_BRANCH) {
            getLog().debug("[{}] repository '{}' branch '{}' exists", vcsUrlHost, repository, branch)
            parent?.let {
                throw IllegalArgumentException("[$vcsUrlHost] repository '$repository' branch '$branch' exists already, but parent '$it' is specified")
            }
        } else {
            getLog().debug(
                "[{}] create repository '{}' branch '{}' from parent '{}'",
                vcsUrlHost,
                repository,
                branch,
                parent ?: DEFAULT_BRANCH
            )
            parent?.let { parentValue ->
                gitCheckout(git, parentValue)
            }
            retryableExecution {
                git.branchCreate().setName(branch).call()
            }
        }
        gitCheckout(git, branch)
        return git
    }

    private fun gitCheckout(git: Git, commitId: String) {
        try {
            retryableExecution {
                git.checkout().setName(commitId).call()
            }
        } catch (e: RefNotFoundException) {
            throw IllegalArgumentException("Target '$commitId' is not found in repository")
        }
    }

    /**
     * @param retries number of retries
     * @param pingInterval interval between retries in milliseconds
     * @param raiseOnException if true, exception will be raised after retries
     * @param waitMessage message to log before each retry
     * @param failMessage message to log after all retries(%d will be replaced with elapsed time)
     */
    private fun wait(
        retries: Int,
        pingInterval: Long,
        raiseOnException: Boolean,
        waitMessage: String,
        failMessage: String?,
        checkFunc: () -> Unit
    ) {
        var exception: Exception? = null
        val start = System.currentTimeMillis()
        for (i in 1..retries) {
            try {
                checkFunc()
                return
            } catch (e: Exception) {
                exception = e
                getLog().warn("$waitMessage, retries remained: ${retries - i}")
                TimeUnit.MILLISECONDS.sleep(pingInterval)
            }
        }
        if (exception != null && raiseOnException) {
            val elapsed = (System.currentTimeMillis() - start) / 1000
            val msg = failMessage?.format(elapsed) ?: "Waiting for $elapsed sec was unsuccessful"
            throw IllegalStateException(msg, exception)
        }
    }

    private fun <T> retryableExecution(attemptLimit: Int = 10, attemptIntervalSec: Long = 1, func: () -> T): T {
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

    protected data class Repository(
        val group: String, val name: String, val sshUrl: String
    ) {
        val path = "$group/$name"

        override fun toString() = path
    }

    companion object {
        const val DEFAULT_BRANCH = "master"
        const val INITIAL_COMMIT_MESSAGE = "initial commit"
    }
}
