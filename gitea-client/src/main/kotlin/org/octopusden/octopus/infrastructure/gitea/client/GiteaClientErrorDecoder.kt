package org.octopusden.octopus.infrastructure.gitea.client

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.http.HttpStatus
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaExceptionResponse
import org.octopusden.octopus.infrastructure.gitea.client.exception.NotFoundException

class GiteaClientErrorDecoder(private val objectMapper: ObjectMapper) : ErrorDecoder {
    override fun decode(methodKey: String?, response: Response): Exception {
        val errorResponse = response.body().use { body ->
            body.asInputStream()
                .use { inputStream -> objectMapper.readValue(inputStream, GiteaExceptionResponse::class.java) }
        }
        return if (response.status() == HttpStatus.SC_NOT_FOUND ||
            (response.status() == HttpStatus.SC_INTERNAL_SERVER_ERROR &&
                    errorResponse.message.contains("object does not exist", true))
        ) {
            NotFoundException(errorResponse.message)
        } else {
            IllegalStateException(errorResponse.message)
        }
    }
}
