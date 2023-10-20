package org.octopusden.octopus.infrastructure.common.test

import java.io.File
import org.octopusden.octopus.infrastructure.common.test.dto.ChangeSet
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet


interface TestClient {
    fun commit(newChangeSet: NewChangeSet, parent: String? = null): ChangeSet
    fun tag(vcsUrl: String, commitId: String, tag: String)
    fun exportRepository(vcsUrl: String, zip: File)
    fun importRepository(vcsUrl: String, zip: File)
    fun getCommits(vcsUrl: String, branch: String? = null): List<ChangeSet>
    fun clearData()
}
