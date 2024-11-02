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
                    val targetResponse =
                        errorResponseTypes.getOrDefault(methodKey, ArtifactoryErrorsResponse::class.java)
                    inputStream.readBytes().let { bytes ->
                        try {
                            objectMapper.readValue(bytes, targetResponse).toString()
                        } catch (e: Exception) {
                            String(bytes)
                        }
                    }
                }
            when (closableResponse.status()) {
                HttpStatus.SC_NOT_FOUND -> throw NotFoundException(message)
                else -> InternalServerError(message)
            }
        }
    }

    companion object {
        private val errorResponseTypes =
            mapOf<String, Class<*>>("ArtifactoryClient#promoteBuild(String,String,PromoteBuildRequest)" to ArtifactoryResponse::class.java)
    }
}
