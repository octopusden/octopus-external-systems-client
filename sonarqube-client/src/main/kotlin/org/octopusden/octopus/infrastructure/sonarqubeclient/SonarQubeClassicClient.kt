package org.octopusden.octopus.infrastructure.sonarqubeclient

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.Logger
import feign.RequestInterceptor
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.sonarqubeclient.dto.SonarQubeComponentList
import org.octopusden.octopus.infrastructure.sonarqubeclient.dto.SonarQubeMeasureList

class SonarQubeClassicClient(
    apiParametersProvider: ClientParametersProvider,
    mapper: ObjectMapper
) : SonarQubeClient {

    private val client: SonarQubeClient = createClient(
        apiParametersProvider.getApiUrl(),
        apiParametersProvider.getAuth().getInterceptor(),
        mapper
    )

    constructor(apiParametersProvider: ClientParametersProvider) : this(
        apiParametersProvider,
        getMapper()
    )

    override fun getProjects(requestParams: Map<String, Any>): SonarQubeComponentList {
        return client.getProjects(requestParams)
    }

    override fun getMetricsHistory(requestParams: Map<String, Any>): SonarQubeMeasureList {
        return client.getMetricsHistory(requestParams)
    }

    companion object {
        private fun getMapper() = jacksonObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        private fun createClient(apiUrl: String, interceptor: RequestInterceptor, objectMapper: ObjectMapper) =
            Feign.builder()
                .client(ApacheHttpClient())
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .requestInterceptor(interceptor)
                .logger(Slf4jLogger(SonarQubeClient::class.java))
                .logLevel(Logger.Level.FULL)
                .target(SonarQubeClient::class.java, apiUrl)

    }
}
