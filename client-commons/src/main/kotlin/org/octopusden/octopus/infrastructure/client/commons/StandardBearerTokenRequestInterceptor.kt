package org.octopusden.octopus.infrastructure.client.commons

import feign.RequestInterceptor
import feign.RequestTemplate
import org.apache.http.HttpHeaders

open class StandardBearerTokenRequestInterceptor(private val token: String) : RequestInterceptor {
    override fun apply(template: RequestTemplate) {
        template.header(HttpHeaders.AUTHORIZATION, "Bearer $token")
    }
}
