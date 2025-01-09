package org.octopusden.octopus.infrastructure.bitbucket.client

import com.fasterxml.jackson.core.JsonProcessingException
import feign.Response
import feign.codec.Decoder
import java.lang.reflect.Type
import com.fasterxml.jackson.databind.ObjectMapper
import feign.jackson.JacksonDecoder
import java.io.ByteArrayInputStream
import java.io.InputStreamReader

class BitbucketClientResponseDecoder(objectMapper: ObjectMapper) : Decoder {
    private val jsonDecoder = JacksonDecoder(objectMapper)

    override fun decode(response: Response, type: Type): Any {
        val bodyBytes = response.body()?.asInputStream()?.readBytes()
            ?: throw IllegalStateException("Empty response body while decoding")

        return try {
            val bufferedResponse = response.toBuilder().body(ByteArrayInputStream(bodyBytes), bodyBytes.size).build()
            jsonDecoder.decode(bufferedResponse, type)
        } catch (e: JsonProcessingException) {
            InputStreamReader(ByteArrayInputStream(bodyBytes), Charsets.UTF_8).use { reader ->
                reader.readText()
            }
        }
    }
}