package org.octopusden.octopus.infrastructure.artifactory.client

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.http.HttpStatus
import org.octopusden.octopus.infrastructure.artifactory.client.dto.ArtifactoryErrorsResponse
import org.octopusden.octopus.infrastructure.artifactory.client.dto.ArtifactoryResponse
import org.octopusden.octopus.infrastructure.artifactory.client.exception.InternalServerError
import org.octopusden.octopus.infrastructure.artifactory.client.exception.NotFoundException

class ArtifactoryClientErrorDecoder(private val objectMapper: ObjectMapper) : ErrorDecoder {
    override fun decode(methodKey: String, response: Response): Exception {
        return response.use { closableResponse ->
            val message = closableResponse.body()
                .asInputStream()
                .use { inputStream ->
                    inputStream.readBytes().let { bytes ->
                        var deserializedErrors: String? = null
                        for (type in arrayOf(ArtifactoryErrorsResponse::class, ArtifactoryResponse::class)) {
                            try {
                                deserializedErrors = objectMapper.readValue(bytes, type.java).toString()
                                break
                            } catch (_: Exception) {
                            }
                        }
                        deserializedErrors ?: String(bytes)
                    }
                }
            when (closableResponse.status()) {
                HttpStatus.SC_NOT_FOUND -> throw NotFoundException(message)
                else -> InternalServerError(message)
            }
        }
    }
}
