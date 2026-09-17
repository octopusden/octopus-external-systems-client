package org.octopusden.octopus.infrastructure.artifactory.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.Logger
import feign.RequestInterceptor
import feign.Response
import feign.codec.Encoder
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.octopusden.octopus.infrastructure.artifactory.client.dto.AqlSearchResponse
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfo
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfoResponse
import org.octopusden.octopus.infrastructure.artifactory.client.dto.DeleteBuildRequest
import org.octopusden.octopus.infrastructure.artifactory.client.dto.PromoteBuildRequest
import org.octopusden.octopus.infrastructure.artifactory.client.dto.PromoteDockerImageRequest
import org.octopusden.octopus.infrastructure.artifactory.client.dto.SystemVersion
import org.octopusden.octopus.infrastructure.artifactory.client.dto.Tokens
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import java.nio.charset.StandardCharsets

@Suppress("unused")
class ArtifactoryClassicClient(
    clientParametersProvider: ClientParametersProvider,
    mapper: ObjectMapper = getMapper(),
) : ArtifactoryClient {
    private val errorDecoder = ArtifactoryClientErrorDecoder(mapper)
    private val client: ArtifactoryClient =
        createClient(
            clientParametersProvider.getApiUrl(),
            clientParametersProvider.getAuth().getInterceptor(),
            mapper,
            errorDecoder,
        )
    private val downloadClient: ArtifactoryClient =
        createDownloadClient(
            clientParametersProvider.getApiUrl(),
            clientParametersProvider.getAuth().getInterceptor(),
            mapper,
        )

    constructor(apiParametersProvider: ClientParametersProvider) : this(
        apiParametersProvider,
        getMapper(),
    )

    override fun getVersion(): SystemVersion = client.getVersion()

    override fun getTokens(): Tokens = client.getTokens()

    override fun getBuildInfo(
        buildName: String,
        buildNumber: String,
    ): BuildInfoResponse = client.getBuildInfo(buildName, buildNumber)

    override fun uploadBuildInfo(request: BuildInfo) = client.uploadBuildInfo(request)

    override fun deleteBuild(request: DeleteBuildRequest) = client.deleteBuild(request)

    override fun promoteBuild(
        buildName: String,
        buildNumber: String,
        request: PromoteBuildRequest,
    ) = client.promoteBuild(buildName, buildNumber, request)

    override fun promoteDockerImage(
        repoKey: String,
        request: PromoteDockerImageRequest,
    ) = client.promoteDockerImage(repoKey, request)

    override fun searchByAQL(query: String): AqlSearchResponse = client.searchByAQL(query)

    override fun downloadArtifact(artifactPath: String): Response {
        val response = downloadClient.downloadArtifact(artifactPath)
        if (response.status() in 200..299) return response
        throw errorDecoder.decode("ArtifactoryClient#downloadArtifact(String)", response)
    }

    companion object {
        private fun getMapper(): ObjectMapper {
            val objectMapper = jacksonObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            objectMapper.setDateFormat(StdDateFormat().withColonInTimeZone(false))
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            return objectMapper
        }

        private fun buildFeignBuilder(
            interceptor: RequestInterceptor,
            objectMapper: ObjectMapper,
            logLevel: Logger.Level,
        ): Feign.Builder {
            val jacksonEncoder: Encoder = JacksonEncoder(objectMapper)
            return Feign
                .builder()
                .client(ApacheHttpClient())
                .encoder { body, bodyType, template ->
                    if (body is String) {
                        template.body(body.toByteArray(StandardCharsets.UTF_8), StandardCharsets.UTF_8)
                    } else {
                        jacksonEncoder.encode(body, bodyType, template)
                    }
                }.decoder(JacksonDecoder(objectMapper))
                .requestInterceptor(interceptor)
                .logger(Slf4jLogger(ArtifactoryClient::class.java))
                .logLevel(logLevel)
        }

        private fun createClient(
            apiUrl: String,
            interceptor: RequestInterceptor,
            objectMapper: ObjectMapper,
            errorDecoder: ArtifactoryClientErrorDecoder,
        ): ArtifactoryClient =
            buildFeignBuilder(interceptor, objectMapper, Logger.Level.FULL)
                .errorDecoder(errorDecoder)
                .target(ArtifactoryClient::class.java, apiUrl)

        private fun createDownloadClient(
            apiUrl: String,
            interceptor: RequestInterceptor,
            objectMapper: ObjectMapper,
        ): ArtifactoryClient =
            buildFeignBuilder(interceptor, objectMapper, Logger.Level.NONE)
                .target(ArtifactoryClient::class.java, apiUrl)
    }
}
