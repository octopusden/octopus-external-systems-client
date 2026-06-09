package org.octopusden.octopus.infrastructure.gitea.client

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.http.HttpStatus
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaExceptionResponse
import org.octopusden.octopus.infrastructure.gitea.client.exception.NotFoundException

class GiteaClientErrorDecoder(private val objectMapper: ObjectMapper) : ErrorDecoder {
    override fun decode(methodKey: String?, response: Response): Exception {
        val rawBody = response.body()?.use { body ->
            body.asInputStream().use { it.readBytes() }
        } ?: ByteArray(0)
        val message = runCatching {
            objectMapper.readValue(rawBody, GiteaExceptionResponse::class.java).message
        }.getOrNull() ?: rawBody.toString(Charsets.UTF_8).take(500)

        val status = response.status()
        return when {
            status == HttpStatus.SC_NOT_FOUND -> NotFoundException(message)
            status == HttpStatus.SC_INTERNAL_SERVER_ERROR &&
                message.contains("object does not exist", true) -> NotFoundException(message)
            else -> IllegalStateException("HTTP $status: $message")
        }
    }
}
