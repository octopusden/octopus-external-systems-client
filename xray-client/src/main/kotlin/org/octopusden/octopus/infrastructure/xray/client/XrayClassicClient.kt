package org.octopusden.octopus.infrastructure.xray.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Client
import feign.Feign
import feign.Logger
import feign.RequestInterceptor
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.xray.client.dto.ArtifactStatusRequest
import org.octopusden.octopus.infrastructure.xray.client.dto.ArtifactSummaryRequest
import org.octopusden.octopus.infrastructure.xray.client.dto.ComponentSummaryRequest

/** [ClientParametersProvider.getApiUrl] is the JFrog platform URL, the one Artifactory is served under too. */
class XrayClassicClient(
    clientParametersProvider: ClientParametersProvider,
    mapper: ObjectMapper = getMapper(),
    feignClient: Client = ApacheHttpClient(),
) : XrayClient {
    private val client: XrayClient =
        createClient(
            clientParametersProvider.getApiUrl(),
            clientParametersProvider.getAuth().getInterceptor(),
            mapper,
            feignClient,
        )

    constructor(apiParametersProvider: ClientParametersProvider) : this(
        apiParametersProvider,
        getMapper(),
    )

    override fun getArtifactStatus(request: ArtifactStatusRequest) = client.getArtifactStatus(request)

    override fun getArtifactSummary(request: ArtifactSummaryRequest) = client.getArtifactSummary(request)

    override fun getComponentSummary(request: ComponentSummaryRequest) = client.getComponentSummary(request)

    companion object {
        private fun getMapper(): ObjectMapper =
            jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)

        private fun createClient(
            apiUrl: String,
            interceptor: RequestInterceptor,
            objectMapper: ObjectMapper,
            feignClient: Client,
        ): XrayClient =
            Feign
                .builder()
                .client(feignClient)
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .errorDecoder(XrayClientErrorDecoder())
                .requestInterceptor(interceptor)
                .logger(Slf4jLogger(XrayClient::class.java))
                .logLevel(Logger.Level.BASIC)
                .target(XrayClient::class.java, apiUrl)
    }
}
