package org.octopusden.octopus.infrastructure.bitbucket.client

import org.octopusden.octopus.infrastructure.client.commons.StandardBearerTokenRequestInterceptor
import feign.RequestTemplate

class BitbucketBearerTokenRequestInterceptor(token: String) : StandardBearerTokenRequestInterceptor(token) {
    override fun apply(template: RequestTemplate) {
        super.apply(template)
        template.header("X-Atlassian-Token", "no-check")
    }
}
