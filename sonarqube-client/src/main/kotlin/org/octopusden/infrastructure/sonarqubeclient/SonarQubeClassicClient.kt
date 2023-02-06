package org.octopusden.infrastructure.sonarqubeclient

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.octopusden.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.infrastructure.sonarqubeclient.dto.SonarQubeEntityList
import org.octopusden.infrastructure.sonarqubeclient.dto.SonarQubeComponent
import feign.Feign
import feign.Logger
import feign.RequestInterceptor
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger

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

    override fun getProjects(requestParams: Map<String, Any>): SonarQubeEntityList<SonarQubeComponent> {
        return client.getProjects(requestParams)
    }

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
        ): SonarQubeClient {
            return Feign.builder()
                .client(ApacheHttpClient())
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
//            .errorDecoder(SonarQubeClientErrorDecoder(objectMapper))
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .requestInterceptor(interceptor)
                .logger(Slf4jLogger(SonarQubeClient::class.java))
                .logLevel(Logger.Level.FULL)
                .target(SonarQubeClient::class.java, apiUrl)
        }
    }
}
