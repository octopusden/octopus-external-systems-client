package org.octopusden.octopus.infrastructure.bitbucket.client

import feign.Param.Expander

class BitbucketCommitIdValidator : Expander {
    override fun expand(value: Any?) =
        if (value is String && COMMIT_ID_REGEX matches value) {
            value
        } else {
            throw IllegalArgumentException("'$value' is not valid BitBucket commit id")
        }

    companion object {
        private val COMMIT_ID_REGEX = "[0-9a-fA-F]+".toRegex()
    }
}