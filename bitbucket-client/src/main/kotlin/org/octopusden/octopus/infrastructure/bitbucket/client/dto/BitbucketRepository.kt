package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class BitbucketRepository
    constructor(
        val id: Long,
        val name: String,
        val slug: String,
        val links: BitbucketLinks,
        val project: BitbucketProject,
        val archived: Boolean? = null,
    ) {
        var description: String? = null
            private set
        var public: Boolean? = null
            private set

        @JsonCreator
        constructor(
            id: Long,
            name: String,
            slug: String,
            links: BitbucketLinks,
            project: BitbucketProject,
            archived: Boolean?,
            description: String?,
            public: Boolean?,
        ) : this(id, name, slug, links, project, archived) {
            this.description = description
            this.public = public
        }
    }
