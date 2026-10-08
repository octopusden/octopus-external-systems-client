package org.octopusden.octopus.infrastructure.common.test

import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.transport.PreReceiveHook
import org.eclipse.jgit.transport.ReceivePack
import org.eclipse.jgit.transport.TestProtocol
import org.eclipse.jgit.transport.Transport
import org.eclipse.jgit.transport.UploadPack
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.eclipse.jgit.lib.Repository as JGitRepository

/**
 * A push that fails after the server already updated the branch: the retry is then a no-op, and a
 * server like Bitbucket never runs its post-push processing for that commit. The client must push
 * the commit again as a real update.
 */
class BaseTestClientPushTest {
    private val server = FakeServer()
    private val client = FakeServerClient(server)

    @AfterEach
    fun tearDown() = server.close()

    @Test
    fun `a clean push is not repeated`() {
        client.commit(NewChangeSet("first", REPOSITORY, "master"))
        val second = client.commit(NewChangeSet("second", REPOSITORY, "master"))

        assertEquals(second.id, server.head("master"))
        assertEquals(2, server.updates.size)
    }

    @Test
    fun `a push that failed after updating the branch is pushed again`() {
        val first = client.commit(NewChangeSet("first", REPOSITORY, "master"))
        server.failNextPushAfterUpdate = true
        val second = client.commit(NewChangeSet("second", REPOSITORY, "master"))

        assertEquals(second.id, server.head("master"))
        // The failed push is never processed; the redo is: back to the parent, then forward again.
        assertEquals(listOf(null to first.id, second.id to first.id, first.id to second.id), server.updates)
    }

    @Test
    fun `a branch created by the failed push is deleted and pushed again`() {
        val base = client.commit(NewChangeSet("base", REPOSITORY, "master"))
        server.failNextPushAfterUpdate = true
        val feature = client.commit(NewChangeSet("feature", REPOSITORY, "feature"), base.id)

        assertEquals(feature.id, server.head("feature"))
        assertEquals(listOf(null to base.id, feature.id to null, null to feature.id), server.updates)
    }

    @Test
    fun `a redo the server rejects fails the commit`() {
        client.commit(NewChangeSet("first", REPOSITORY, "master"))
        server.failNextPushAfterUpdate = true
        server.allowNonFastForwards = false

        val e = assertThrows(IllegalStateException::class.java) {
            client.commit(NewChangeSet("second", REPOSITORY, "master"))
        }
        assertTrue(e.message!!.contains("again failed (rewind: REJECTED_OTHER_REASON"), e.message)
    }

    /** One in-memory bare repository served over jgit's in-process [TestProtocol]. */
    class FakeServer : AutoCloseable {
        val repository: JGitRepository = InMemoryRepository(DfsRepositoryDescription("fake"))
        var failNextPushAfterUpdate = false
        var allowNonFastForwards = true

        /** (old, new) of every branch update the server processed; null for a missing side. */
        val updates = mutableListOf<Pair<String?, String?>>()

        private val protocol = TestProtocol<Any?>(
            { _, db -> UploadPack(db) },
            { _, db -> receivePack(db) },
        )
        val url: String

        init {
            Transport.register(protocol)
            url = protocol.register(null, repository).toString()
        }

        fun head(branch: String): String? = repository.exactRef("refs/heads/$branch")?.objectId?.name

        private fun receivePack(db: JGitRepository) =
            ReceivePack(db).apply {
                isAllowNonFastForwards = allowNonFastForwards
                preReceiveHook = if (failNextPushAfterUpdate) {
                    failNextPushAfterUpdate = false
                    // Update the refs, then break the session: the server never processes this push.
                    PreReceiveHook { pack, commands ->
                        commands.forEach { it.execute(pack) }
                        error("simulated server failure after the ref update")
                    }
                } else {
                    // Only commands that passed validation get here, so a rejected update is not recorded.
                    PreReceiveHook { _, commands ->
                        commands.forEach { updates += it.oldId.nameOrNull() to it.newId.nameOrNull() }
                    }
                }
            }

        private fun ObjectId.nameOrNull() = if (this == ObjectId.zeroId()) null else name

        override fun close() = Transport.unregister(protocol)
    }

    class FakeServerClient(
        private val server: FakeServer,
    ) : BaseTestClient("http://fake", "user", "password", commitRetries = 1, commitPingInterval = 0) {
        override val vcsUrlRegex = "(?:ssh://)?git@$vcsUrlHost/([^/]+)/([^/]+).git".toRegex()

        override fun Repository.getUrl() = server.url

        override fun getLog(): Logger = LoggerFactory.getLogger(BaseTestClientPushTest::class.java)

        override fun checkActive() = Unit

        override fun createRepository(repository: Repository) = Unit

        override fun deleteRepository(repository: Repository) = Unit

        override fun setRepositoryArchived(
            repository: Repository,
            archived: Boolean,
        ) = Unit

        override fun checkCommit(
            repository: Repository,
            sha: String,
        ) {
            checkNotNull(server.repository.parseCommit(ObjectId.fromString(sha)))
        }
    }

    companion object {
        private const val REPOSITORY = "ssh://git@fake/group/repo.git"
    }
}
