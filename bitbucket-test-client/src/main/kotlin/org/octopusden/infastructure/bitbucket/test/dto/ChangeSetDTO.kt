package org.octopusden.infastructure.bitbucket.test.dto

import java.util.*

open class NewChangeSet(
    val message: String,
    val repository: String,
    val branch: String,
) {
    override fun toString(): String {
        return "NewChangeSet(message='$message', repository='$repository', branch='$branch')"
    }
}

class ChangeSet(val id: String, message: String, repository: String, branch: String, val author: String, val authorDate: Date) :
    NewChangeSet(message, repository, branch) {
    override fun toString(): String {
        return "ChangeSet(id='$id', author=$author, authorDate=$authorDate, message='$message', repository='$repository', branch='$branch') ${super.toString()}"
    }
}
