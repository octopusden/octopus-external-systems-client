package org.octopusden.octopus.infrastructure.bitbucket.client

import feign.RequestTemplate
import org.octopusden.octopus.infrastructure.client.commons.StandardBearerTokenRequestInterceptor

class BitbucketBearerTokenRequestInterceptor(
    token: String,
) : StandardBearerTokenRequestInterceptor(token) {
    override fun apply(template: RequestTemplate) {
        super.apply(template)
        template.header("X-Atlassian-Token", "no-check")
    }
}
