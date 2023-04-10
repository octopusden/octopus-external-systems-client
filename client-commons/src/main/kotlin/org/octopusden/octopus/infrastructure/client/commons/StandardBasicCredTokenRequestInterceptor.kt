package org.octopusden.octopus.infrastructure.client.commons

import feign.RequestInterceptor
import feign.RequestTemplate
import org.apache.http.HttpHeaders
import java.util.Base64

open class StandardBasicCredTokenRequestInterceptor(private val username: String, private val password: String) :
    RequestInterceptor {
    override fun apply(template: RequestTemplate) {
        template.header(
            HttpHeaders.AUTHORIZATION,
            "Basic ${Base64.getEncoder().encodeToString("$username:$password".toByteArray())}"
        )
    }
}
