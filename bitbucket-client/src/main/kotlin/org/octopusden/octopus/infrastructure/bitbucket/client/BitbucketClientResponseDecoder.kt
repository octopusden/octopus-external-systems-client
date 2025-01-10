package org.octopusden.octopus.infrastructure.bitbucket.client

import feign.Response
import feign.codec.Decoder
import java.lang.reflect.Type
import com.fasterxml.jackson.databind.ObjectMapper
import feign.jackson.JacksonDecoder
import java.io.ByteArrayInputStream

class BitbucketClientResponseDecoder(objectMapper: ObjectMapper) : Decoder {
    private val jsonDecoder = JacksonDecoder(objectMapper)

    override fun decode(response: Response, type: Type): Any {
        val bodyBytes = response.body()?.asInputStream()?.readBytes()
            ?: throw IllegalStateException("Empty response body while decoding")

        return if (type == String::class.java) {
            String(bodyBytes, Charsets.UTF_8)
        } else {
            val bufferedResponse = response.toBuilder()
                .body(ByteArrayInputStream(bodyBytes), bodyBytes.size)
                .build()
            jsonDecoder.decode(bufferedResponse, type)
        }
    }
}