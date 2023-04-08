package org.octopusden.octopus.infrastructure.bitbucket.client

import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredTokenRequestInterceptor
import feign.RequestTemplate

class BitbucketBasicCredTokenRequestInterceptor(username: String, password: String) :
    StandardBasicCredTokenRequestInterceptor(username, password) {
    override fun apply(template: RequestTemplate) {
        super.apply(template)
        template.header("X-Atlassian-Token", "no-check")
    }
}
