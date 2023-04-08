package org.octopusden.octopus.infrastructure.bitbucket.client

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.ErrorDecoder
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketExceptionsResponse

class BitbucketClientErrorDecoder(private val objectMapper: ObjectMapper) : ErrorDecoder {

    override fun decode(methodKey: String?, response: Response): Exception {
        return response.use { closableResponse ->
            closableResponse.body()
                .asInputStream()
                .use { inputStream ->
                    val en = objectMapper.readValue(inputStream, BitbucketExceptionsResponse::class.java)
                    en.errors.firstOrNull()
                        ?.let { bitbucketException ->
                            val message = bitbucketException.message
                            bitbucketException.exceptionName
                                ?.exceptionSupplier
                                ?.invoke(message) ?: IllegalStateException(message)
                        } ?: IllegalStateException()
                }
        }
    }
}
