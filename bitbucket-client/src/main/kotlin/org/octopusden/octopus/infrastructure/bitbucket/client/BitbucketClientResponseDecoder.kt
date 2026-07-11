package org.octopusden.octopus.infrastructure.bitbucket.client

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.codec.Decoder
import feign.jackson.JacksonDecoder
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketPlainTextResponse
import java.io.ByteArrayInputStream
import java.lang.reflect.Type

class BitbucketClientResponseDecoder(
    objectMapper: ObjectMapper,
) : Decoder {
    private val jsonDecoder = JacksonDecoder(objectMapper)

    override fun decode(
        response: Response,
        type: Type,
    ): Any {
        val bodyBytes = response.body()?.asInputStream()?.readBytes()
            ?: throw IllegalStateException("Empty response body while decoding")

        return if (type == BitbucketPlainTextResponse::class.java) {
            BitbucketPlainTextResponse(String(bodyBytes, Charsets.UTF_8))
        } else {
            val bufferedResponse = response
                .toBuilder()
                .body(ByteArrayInputStream(bodyBytes), bodyBytes.size)
                .build()
            jsonDecoder.decode(bufferedResponse, type)
        }
    }
}
