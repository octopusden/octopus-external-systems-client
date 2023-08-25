package org.octopusden.octopus.infrastructure.common.test

import org.octopusden.octopus.infrastructure.common.test.dto.ChangeSet
import org.octopusden.octopus.infrastructure.common.test.dto.NewChangeSet


interface TestClient {
    fun commit(newChangeSet: NewChangeSet, parent: String? = null): ChangeSet
    fun tag(vcsUrl: String, commitId: String, tag: String)
    fun clearData()
}
