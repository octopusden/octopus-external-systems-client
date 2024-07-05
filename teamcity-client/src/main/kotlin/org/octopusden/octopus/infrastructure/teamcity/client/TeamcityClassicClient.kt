package org.octopusden.octopus.infrastructure.teamcity.client

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
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkFeature

class TeamcityClassicClient(
    apiParametersProvider: ClientParametersProvider,
    mapper: ObjectMapper
) : TeamcityClient {

    private val client: TeamcityClient = createClient(
        apiParametersProvider.getApiUrl(),
        apiParametersProvider.getAuth().getInterceptor(),
        mapper
    )

    constructor(apiParametersProvider: ClientParametersProvider) : this(
        apiParametersProvider,
        getMapper()
    )

    companion object {
        private fun getMapper() = jacksonObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }

        private fun createClient(apiUrl: String, interceptor: RequestInterceptor, objectMapper: ObjectMapper) =
            Feign.builder()
                .requestInterceptor { requestTemplate -> requestTemplate?.header("Origin", apiUrl) }
                .client(ApacheHttpClient())
                .encoder(JacksonEncoder(objectMapper))
//                .decoder(JacksonDecoder(objectMapper))
                .decoder(TeamcityClientDecoder(objectMapper))
                .requestInterceptor(interceptor)
                .logger(Slf4jLogger(TeamcityClient::class.java))
                .logLevel(Logger.Level.FULL)
                .target(TeamcityClient::class.java, apiUrl)
    }

    override fun createProject(dto: TeamcityCreateProject) = client.createProject(dto)
    override fun deleteProject(project: String) = client.deleteProject(project)
    override fun getProject(project: String) = client.getProject(project)
    override fun createBuildType(dto: TeamcityCreateBuildType) = client.createBuildType(dto)
    override fun getBuildType(buildType: String) = client.getBuildType(buildType)
    override fun deleteBuildType(buildType: String) = client.deleteBuildType(buildType)
    override fun getBuildTypes() = client.getBuildTypes()
    override fun addBuildTypeFeature(buildType: String, feature: TeamcityLinkFeature) =
        client.addBuildTypeFeature(buildType, feature)
    override fun getBuildTypeFeatures(buildType: String) = client.getBuildTypeFeatures(buildType)
    override fun getBuildTypeFeature(buildType: String, feature: String) = client.getBuildTypeFeature(buildType, feature)
    override fun updateBuildTypeFeatureParameter(
        buildType: String,
        feature: String,
        parameter: String,
        newValue: String
    ) = client.updateBuildTypeFeatureParameter(buildType, feature, parameter, newValue)
    override fun getBuildTypeFeatureParameter(
        buildType: String,
        feature: String,
        parameter: String
    ) = client.getBuildTypeFeatureParameter(buildType, feature, parameter)
}
