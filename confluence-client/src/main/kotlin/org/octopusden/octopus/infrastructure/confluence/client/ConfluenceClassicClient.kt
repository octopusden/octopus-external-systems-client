package org.octopusden.octopus.infrastructure.confluence.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.RequestInterceptor
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import feign.Logger
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.confluence.client.dto.ConfluencePage
import org.octopusden.octopus.infrastructure.confluence.client.dto.ConfluencePageCreateRequest
import org.octopusden.octopus.infrastructure.confluence.client.dto.ConfluencePageUpdateRequest
import org.octopusden.octopus.infrastructure.confluence.client.dto.ConfluenceSearchResponse

class ConfluenceClassicClient(
    apiParametersProvider: ClientParametersProvider,
    mapper: ObjectMapper
) : ConfluenceClient {

    private val client: ConfluenceClient = createClient(
        apiParametersProvider.getApiUrl(),
        apiParametersProvider.getAuth().getInterceptor(),
        mapper
    )

    constructor(apiParametersProvider: ClientParametersProvider) : this(
        apiParametersProvider,
        getMapper()
    )

    override fun getPageById(id: String, queryParams: Map<String, String>): ConfluencePage =
        client.getPageById(id, queryParams)

    override fun updatePage(id: String, page: ConfluencePageUpdateRequest): ConfluencePage = client.updatePage(id, page)

    override fun searchPages(queryParams: Map<String, String>): ConfluenceSearchResponse = client.searchPages(queryParams)

    override fun createPage(request: ConfluencePageCreateRequest): ConfluencePage = client.createPage(request)

    companion object {
        private fun getMapper(): ObjectMapper {
            val objectMapper = jacksonObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            return objectMapper
        }

        private fun createClient(
            apiUrl: String,
            interceptor: RequestInterceptor,
            objectMapper: ObjectMapper
        ) = Feign.builder()
            .client(ApacheHttpClient())
            .encoder(JacksonEncoder(objectMapper))
            .decoder(JacksonDecoder(objectMapper))
            .requestInterceptor(interceptor)
            .logger(Slf4jLogger(ConfluenceClient::class.java))
            .logLevel(Logger.Level.BASIC)
            .target(ConfluenceClient::class.java, apiUrl)
    }
}