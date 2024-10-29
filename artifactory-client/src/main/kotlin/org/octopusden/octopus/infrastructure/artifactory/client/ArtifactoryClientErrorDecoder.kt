package org.octopusden.octopus.infrastructure.artifactory.client

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.http.HttpStatus
import org.octopusden.octopus.infrastructure.artifactory.client.dto.ArtifactoryErrorsResponse
import org.octopusden.octopus.infrastructure.artifactory.client.exception.InternalServerError
import org.octopusden.octopus.infrastructure.artifactory.client.exception.NotFoundException

class ArtifactoryClientErrorDecoder(private val objectMapper: ObjectMapper) : ErrorDecoder {
    override fun decode(methodKey: String?, response: Response): Exception {
        return response.use { closableResponse ->
            val message = closableResponse.body()
                .asInputStream()
                .use { inputStream ->
                    try {
                        objectMapper.readValue(
                            inputStream,
                            ArtifactoryErrorsResponse::class.java

                        ).errors.joinToString { (status, message) -> "$message ($status)" }
                    } catch (e: Exception) {
                        ""
                    }
                }
            when (closableResponse.status()) {
                HttpStatus.SC_NOT_FOUND -> throw NotFoundException(message)
                else -> InternalServerError(message)
            }
        }
    }
}
