package org.octopusden.octopus.infrastructure.common.test

import java.io.File
import org.octopusden.octopus.infrastructure.common.test.dto.ChangeSet
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet
import java.nio.file.Path


interface TestClient {
    fun commit(newChangeSet: NewChangeSet, parent: String? = null, filesToCommit: List<Path>? = null): ChangeSet
    fun tag(vcsUrl: String, commitId: String, tag: String)
    fun exportRepository(vcsUrl: String, zip: File)
    fun importRepository(vcsUrl: String, zip: File)
    fun getCommits(vcsUrl: String, branch: String): List<ChangeSet>
    fun clearData(): Collection<String>
    fun wait( retries: Int,  pingInterval: Long,  raiseOnException: Boolean,  waitMessage: String,  failMessage: String?,  checkFunc: () -> Unit)
}
