package org.octopusden.infrastructure.client.commons

import feign.RequestInterceptor

abstract class CredentialProvider constructor(private val func: () -> RequestInterceptor) {
    fun getInterceptor(): RequestInterceptor = func.invoke()
}
