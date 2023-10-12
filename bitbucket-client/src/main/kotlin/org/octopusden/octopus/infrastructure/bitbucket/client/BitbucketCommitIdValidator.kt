package org.octopusden.octopus.infrastructure.bitbucket.client

import feign.Param.Expander

class BitbucketCommitIdValidator : Expander {
    override fun expand(value: Any?) =
        if (value is String && commitIdRegex matches value) {
            value
        } else {
            throw IllegalArgumentException("'$value' is not valid BitBucket commit id")
        }

    companion object {
        private val commitIdRegex = "[0-9a-fA-F]+".toRegex()
    }
}