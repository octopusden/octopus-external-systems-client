package org.octopusden.octopus.infrastructure.teamcity.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.Logger
import feign.RequestInterceptor
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRootEntry
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkFeature
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperty
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependencies
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependency
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityStep
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootEntries
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootEntry

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
                .decoder(TeamcityClientDecoder(objectMapper))
                .requestInterceptor(interceptor)
                .logger(Slf4jLogger(TeamcityClient::class.java))
                .logLevel(Logger.Level.FULL)
                .target(TeamcityClient::class.java, apiUrl)
    }

    override fun createProject(dto: TeamcityCreateProject) =
        client.createProject(dto)

    override fun deleteProject(project: String) =
        client.deleteProject(project)

    override fun getProject(project: String) =
        client.getProject(project)

    override fun createBuildType(dto: TeamcityCreateBuildType) =
        client.createBuildType(dto)

    override fun createBuildType(project: String, buildTypeName: String) =
        client.createBuildType(project, buildTypeName)

    override fun getBuildType(buildType: String) =
        client.getBuildType(buildType)

    override fun deleteBuildType(buildType: String) =
        client.deleteBuildType(buildType)

    override fun getBuildTypes() =
        client.getBuildTypes()

    override fun getBuildTypes(project: String) =
        client.getBuildTypes(project)

    override fun addBuildTypeFeature(buildType: String, feature: TeamcityLinkFeature) =
        client.addBuildTypeFeature(buildType, feature)

    override fun getBuildTypeFeatures(buildType: String) =
        client.getBuildTypeFeatures(buildType)

    override fun getBuildTypeFeature(buildType: String, feature: String) =
        client.getBuildTypeFeature(buildType, feature)

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

    override fun setBuildCounter(buildType: String, newValue: String) =
        client.setBuildCounter(buildType, newValue)

    override fun createSnapshotDependency(buildType: String, dependency: TeamcitySnapshotDependency) =
        client.createSnapshotDependency(buildType, dependency)

    override fun deleteSnapshotDependency(buildType: String, dependency: String) =
        client.deleteSnapshotDependency(buildType, dependency)

    override fun getSnapshotDependencies(buildType: String): TeamcitySnapshotDependencies =
        client.getSnapshotDependencies(buildType)

    override fun disableBuildStep(buildType: String, step: String, newValue: Boolean) =
        client.disableBuildStep(buildType, step, newValue)

    override fun createBuildStep(buildType: String, step: TeamcityStep) =
        client.createBuildStep(buildType, step)

    override fun getBuildSteps(buildType: String) = client.getBuildSteps(buildType)
    override fun createBuildTypeVcsRootEntry(buildType: String, vcsRootEntry: TeamcityCreateVcsRootEntry) =
        client.createBuildTypeVcsRootEntry(buildType, vcsRootEntry)

    override fun deleteBuildTypeVcsRootEntry(buildType: String, vcsRootEntryId: String) =
        client.deleteBuildTypeVcsRootEntry(buildType, vcsRootEntryId)

    override fun getBuildTypeVcsRootEntries(buildType: String): TeamcityVcsRootEntries =
        client.getBuildTypeVcsRootEntries(buildType)

    override fun getBuildTypeVcsRootEntry(buildType: String, vcsRootEntryId: String): TeamcityVcsRootEntry =
        client.getBuildTypeVcsRootEntry(buildType, vcsRootEntryId)

    override fun updateBuildTypeVcsRootEntryCheckoutRules(
        buildType: String,
        vcsRootEntryId: String,
        checkoutRules: String
    ) = client.updateBuildTypeVcsRootEntryCheckoutRules(buildType, vcsRootEntryId, checkoutRules)

    override fun createVcsRoot(vcsRoot: TeamcityCreateVcsRoot) =
        client.createVcsRoot(vcsRoot)

    override fun getVcsRoot(vcsRootId: String): TeamcityVcsRoot =
        client.getVcsRoot(vcsRootId)

    override fun updateVcsRootProperty(vcsRootId: String, propertyName: String, newValue: String) =
        client.updateVcsRootProperty(vcsRootId, propertyName, newValue)

    override fun getVcsRootProperty(vcsRootId: String, propertyName: String) =
        client.getVcsRootProperty(vcsRootId, propertyName)

    override fun getBuildTypeTemplate(buildType: String) =
        client.getBuildTypeTemplate(buildType)

    override fun attachTemplateToBuildType(buildType: String, template: String) =
        client.attachTemplateToBuildType(buildType, template)

    override fun detachTemplatesFromBuildType(buildType: String) =
        client.detachTemplatesFromBuildType(buildType)

    override fun createParameter(type: ConfigurationType, id: String, parameter: TeamcityProperty) =
        client.createParameter(type, id, parameter)

    override fun createParameter(
        type: ConfigurationType,
        id: String,
        parameterName: String,
        value: String
    ) = client.createParameter(type, id, parameterName, value)

    override fun setParameter(
        type: ConfigurationType,
        id: String,
        parameterName: String,
        value: String
    ) = client.setParameter(type, id, parameterName, value)

    override fun getParameter(type: ConfigurationType, id: String, parameterName: String) =
        client.getParameter(type, id, parameterName)

    override fun deleteParameter(type: ConfigurationType, id: String, parameterName: String) =
        client.deleteParameter(type, id, parameterName)
}
