package org.octopusden.octopus.infrastructure.xray.client

import feign.Response
import feign.codec.ErrorDecoder
import org.octopusden.octopus.infrastructure.xray.client.exception.XrayClientException
import org.octopusden.octopus.infrastructure.xray.client.exception.XrayNotFoundException

class XrayClientErrorDecoder : ErrorDecoder {
    override fun decode(
        methodKey: String,
        response: Response,
    ): Exception =
        response.use {
            val body = it
                .body()
                ?.asInputStream()
                ?.use { stream -> String(stream.readBytes()) }
                .orEmpty()
            val message = "$methodKey: HTTP ${it.status()} $body"
            when (it.status()) {
                NOT_FOUND -> XrayNotFoundException(message)
                else -> XrayClientException(it.status(), message)
            }
        }

    private companion object {
        const val NOT_FOUND = 404
    }
}
