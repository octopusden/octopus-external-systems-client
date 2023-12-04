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


abstract class BaseTestClient(username: String,
                              password: String,
                              private val commitRetries: Int,
                              private val commitPingIntervalMsec: Long,
                              private val commitRaiseException: Boolean
    ) : TestClient {

    private val repositories = mutableMapOf<String, Git>()
    private val jgitCredentialsProvider = UsernamePasswordCredentialsProvider(username, password)

    protected abstract fun getLog(): Logger
    protected abstract fun checkActive()
    protected abstract fun convertSshToHttp(vcsUrl: String): String
    protected abstract fun parseUrl(url: String): ProjectRepo
    protected abstract fun createRepository(projectRepo: ProjectRepo)
    protected abstract fun deleteRepository(projectRepo: ProjectRepo)
    protected abstract fun checkCommit(projectRepo: ProjectRepo, sha: String)

    override fun commit(newChangeSet: NewChangeSet, parent: String?): ChangeSet {
        getLog().info("Add commit $newChangeSet${parent?.let { ", parent '$it'" } ?: ""}")
        val branch = newChangeSet.branch
        val vcsUrl = newChangeSet.repository
        val message = newChangeSet.message
        val git = checkout(vcsUrl, branch, parent)
        git.repository.directory.toPath().parent.resolve("${UUID.randomUUID()}.${"commit"}").toFile().createNewFile()
        retryableExecution {
            git.add().addFilepattern(".").call()
        }
        val commit = retryableExecution {
            git.commit().setMessage(message).call()
        }
        retryableExecution {
            git.push().setCredentialsProvider(jgitCredentialsProvider).call()
        }
        wait(waitMessage = "Wait commit='${commit.id.name}' is accessible",
            pingIntervalMsec = commitPingIntervalMsec,
            retries = commitRetries,
            raiseOnException = commitRaiseException,
            failMessage = "Commit: '${commit.id.name}', branch: '${branch}', repository: '${vcsUrl}' is not accessible") {
            checkCommit(parseUrl(newChangeSet.repository), commit.id.name)
        }
        return ChangeSet(
            commit.id.name, commit.fullMessage, vcsUrl, commit.authorIdent.name, commit.authorIdent.`when`
        )
    }


    override fun tag(vcsUrl: String, commitId: String, tag: String) {
        val git = repositories[vcsUrl.lowercase()]
            ?: throw IllegalArgumentException("Repository '$vcsUrl' does not exist, can not tag")
        val projectRepo = parseUrl(vcsUrl)
        getLog().info("Tag commit '$commitId' from $projectRepo as '$tag'")
        gitCheckout(git, commitId)
        retryableExecution {
            git.tag().setName(tag).call()
        }
        retryableExecution {
            git.push().setCredentialsProvider(jgitCredentialsProvider).setPushTags().call()
        }
    }

    override fun exportRepository(vcsUrl: String, zip: File) {
        val git = repositories[vcsUrl.lowercase()]
            ?: throw IllegalArgumentException("Repository '$vcsUrl' does not exist, can not export")
        val projectRepo = parseUrl(vcsUrl)
        getLog().info("Export $projectRepo")
        val repository = git.repository.directory
        ZipOutputStream(zip.outputStream()).use { zipOutputStream ->
            repository.walkTopDown().forEach { file ->
                if (file != repository) {
                    zipOutputStream.putNextEntry(
                        ZipEntry(
                            repository.toPath().relativize(file.toPath()).toString() + if (file.isDirectory) "/" else ""
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
        if (repositories.contains(vcsUrl.lowercase())) {
            throw IllegalArgumentException("Repository '$vcsUrl' exists already, can not import")
        }
        val projectRepo = parseUrl(vcsUrl)
        getLog().info("Import $projectRepo")
        createRepository(projectRepo)
        val repositoryDir = Files.createTempDirectory("TestClient_")
        val repository = repositoryDir.resolve(".git").also { Files.createDirectory(it) }
        ZipInputStream(zip.inputStream()).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (entry.isDirectory) {
                    Files.createDirectory(repository.resolve(entry.name.trimEnd('/')))
                } else {
                    repository.resolve(entry.name).toFile().outputStream().use {
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
        git.remoteAdd().setName("origin").setUri(URIish(convertSshToHttp(vcsUrl))).call()
        retryableExecution {
            git.push().setCredentialsProvider(jgitCredentialsProvider).setPushAll().setPushTags().call()
        }
        val commitId = git.log().call().first().id.name
        wait(waitMessage = "Wait commit '$commitId' is accessible",
            pingIntervalMsec = commitPingIntervalMsec,
            retries = commitRetries,
            raiseOnException = commitRaiseException,
            failMessage = "Commit: '$commitId', repository: '$vcsUrl' is not accessible") {
            checkCommit(projectRepo, commitId)
        }
        repositories[vcsUrl.lowercase()] = git
    }

    override fun getCommits(vcsUrl: String, branch: String?): List<ChangeSet> {
        val git = repositories[vcsUrl.lowercase()]
            ?: throw IllegalArgumentException("Repository '$vcsUrl' does not exist, can not get commits")
        val projectRepo = parseUrl(vcsUrl)
        getLog().info("Get commits from '$projectRepo'${branch?.let { ", branch '$it'" } ?: ""}")
        return git.log().run {
            if (branch.isNullOrBlank()) {
                this.all()
            } else {
                this.add(git.repository.resolve(branch))
            }
        }.call().map {
            ChangeSet(
                it.id.name, it.fullMessage, vcsUrl, it.authorIdent.name, it.authorIdent.`when`
            )
        }
    }

    override fun clearData() {
        repositories.entries.forEach { (vcsUrl, git) ->
            getLog().info("Clear data: $vcsUrl ")
            getLog().debug("Delete git directory: ${git.repository.directory}")
            git.repository.directory.deleteRecursively()
            val projectRepo = parseUrl(vcsUrl)
            getLog().debug("Delete VCS repository: '$projectRepo'")
            deleteRepository(projectRepo)
        }
        repositories.clear()
    }

    private fun getOrCreateRepo(vcsUrl: String): Git {
        return repositories.computeIfAbsent(vcsUrl.lowercase()) { url ->
            getLog().info("Repository $vcsUrl is not prepared, prepare")
            val projectRepo = parseUrl(url)
            createRepository(projectRepo)

            val repositoryDir = Files.createTempDirectory("TestClient_")
            getLog().debug("Clone empty repository to $repositoryDir")

            retryableExecution {
                Git.cloneRepository().setDirectory(repositoryDir.toFile()).setURI(
                    convertSshToHttp(vcsUrl)
                ).setCredentialsProvider(jgitCredentialsProvider).call()
            }
        }
    }

    private fun checkout(vcsUrl: String, branch: String, parent: String?): Git {
        getLog().debug("Checkout $vcsUrl:$branch:${parent ?: ""}")
        val git = getOrCreateRepo(vcsUrl)
        val branches = git.branchList().call()
        if (branches.isEmpty()) {
            getLog().debug("Empty repository, prepare '$DEFAULT_BRANCH'")
            parent?.let { parentValue ->
                throw IllegalArgumentException("Empty repository, parent must be unspecified, but is: '$parentValue'")
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
            getLog().debug("Branch '$branch' exists")
            parent?.let { _ ->
                throw IllegalArgumentException("Use parent only for branch creation, $branch exists")
            }
        } else {
            getLog().debug("Create branch '$branch', parent '${parent ?: "$DEFAULT_BRANCH:head"}'")
            parent?.let { parentValue ->
                gitCheckout(git, parentValue)
            }
            retryableExecution {
                git.branchCreate().setName(branch).call()
            }
        }
        getLog().debug("Checkout $branch")
        gitCheckout(git, branch)
        return git
    }

    private fun gitCheckout(git: Git, commitId: String) {
        try {
            retryableExecution {
                git.checkout().setName(commitId).call()
            }
        } catch (e: RefNotFoundException) {
            throw IllegalArgumentException("Target commit '$commitId' not found")
        }
    }

    private fun wait(retries: Int, pingIntervalMsec: Long, raiseOnException: Boolean, waitMessage: String, failMessage: String?, checkFunc: () -> Unit) {
         var exception: Exception? = null
        for (i in 1..retries) {
            try {
                checkFunc()
                return
            } catch (e: Exception) {
                exception = e
                getLog().warn("$waitMessage, retries remained: ${retries - i}")
                TimeUnit.MILLISECONDS.sleep(pingIntervalMsec)
            }
        }
        if (exception != null && raiseOnException) {
            throw IllegalStateException(failMessage ?: "Waiting for ${retries * pingIntervalMsec / 1000} sec was unsuccessful", exception)
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

    protected data class ProjectRepo(val project: String, val repository: String) {
        val path: String
            get() = "$project/$repository"
    }

    companion object {
        const val DEFAULT_BRANCH = "master"
        const val INITIAL_COMMIT_MESSAGE = "initial commit"
    }
}
