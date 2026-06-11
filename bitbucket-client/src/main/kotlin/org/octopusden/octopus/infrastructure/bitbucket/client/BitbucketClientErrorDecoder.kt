package org.octopusden.octopus.infrastructure.bitbucket.client

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketExceptionsResponse

class BitbucketClientErrorDecoder(private val objectMapper: ObjectMapper) : ErrorDecoder {

    override fun decode(methodKey: String?, response: Response): Exception {
        val rawBody = response.body()?.use { body ->
            body.asInputStream().use { it.readBytes() }
        } ?: ByteArray(0)
        val parsed = runCatching {
            objectMapper.readValue(rawBody, BitbucketExceptionsResponse::class.java)
        }.getOrNull()

        return parsed?.errors?.firstOrNull()?.let { bitbucketException ->
            val message = bitbucketException.message
            bitbucketException.exceptionName?.exceptionSupplier?.invoke(message)
                ?: IllegalStateException(message)
        } ?: IllegalStateException(
            "HTTP ${response.status()}: ${rawBody.toString(Charsets.UTF_8).take(500)}"
        )
    }
}
