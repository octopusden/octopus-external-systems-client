package org.octopusden.octopus.infrastructure.artifactory.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.Logger
import feign.RequestInterceptor
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfoResponse
import org.octopusden.octopus.infrastructure.artifactory.client.dto.PromoteBuild
import org.octopusden.octopus.infrastructure.artifactory.client.dto.SystemVersion
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider

@Suppress("unused")
class ArtifactoryClassicClient(
    clientParametersProvider: ClientParametersProvider, mapper: ObjectMapper = getMapper(),
) : ArtifactoryClient {

    private val client: ArtifactoryClient =
        createClient(
            clientParametersProvider.getApiUrl(),
            clientParametersProvider.getAuth().getInterceptor(),
            mapper
        )

    constructor(apiParametersProvider: ClientParametersProvider) : this(
        apiParametersProvider,
        getMapper()
    )

    override fun getVersion(): SystemVersion = client.getVersion()

    override fun getBuildInfo(buildName: String, buildNumber: String): BuildInfoResponse = client.getBuildInfo(buildName, buildNumber)

    override fun promoteBuild(buildName: String, buildNumber: String, promoteBuild: PromoteBuild) = client.promoteBuild(buildName, buildNumber, promoteBuild)

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
        ): ArtifactoryClient {
            return Feign.builder()
                .client(ApacheHttpClient())
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .errorDecoder(
                    ArtifactoryClientErrorDecoder(
                        objectMapper
                    )
                )
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .requestInterceptor(interceptor)
                .logger(Slf4jLogger(ArtifactoryClient::class.java))
                .logLevel(Logger.Level.FULL)
                .target(ArtifactoryClient::class.java, apiUrl)
        }
    }
}
