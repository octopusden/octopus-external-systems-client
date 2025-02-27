package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes(
    JsonSubTypes.Type(BitbucketTag::class, name = "TAG"),
    JsonSubTypes.Type(BitbucketBranch::class, name = "BRANCH")
)
abstract class BitbucketRef(
    val id: String,
    val displayId: String,
    val latestCommit: String,
    val type: BitbucketRefType,
    val repository: BitbucketRepository? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BitbucketRef) return false
        if (id != other.id) return false
        if (displayId != other.displayId) return false
        if (latestCommit != other.latestCommit) return false
        if (type != other.type) return false
        if (repository != other.repository) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + displayId.hashCode()
        result = 31 * result + latestCommit.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + repository.hashCode()
        return result
    }
}