package org.octopusden.octopus.infrastructure.teamcity.client

import com.fasterxml.jackson.annotation.JsonInclude
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
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProjects
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperty
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependencies
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependency
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityStep
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootEntries
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootEntry
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRoots
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildTypeLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.ProjectLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootInstanceLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootLocator

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
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
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

    override fun getServer() = client.getServer()

    override fun createProject(dto: TeamcityCreateProject) =
        client.createProject(dto)

    override fun deleteProject(locator: ProjectLocator) =
        client.deleteProject(locator)

    fun deleteProject(id: String) =
        deleteProject(ProjectLocator(id = id))

    override fun getProject(locator: ProjectLocator) =
        client.getProject(locator)

    fun getProject(id: String) =
        getProject(ProjectLocator(id = id))

    override fun getProjects(locator: ProjectLocator): TeamcityProjects =
        client.getProjects(locator)

    override fun createBuildType(dto: TeamcityCreateBuildType) =
        client.createBuildType(dto)

    override fun createBuildType(project: ProjectLocator, name: String) =
        client.createBuildType(project, name)

    fun createBuildType(projectId: String, name: String) =
        createBuildType(ProjectLocator(id = projectId), name)

    override fun getBuildType(buildType: BuildTypeLocator) =
        client.getBuildType(buildType)

    fun getBuildType(id: String) =
        getBuildType(BuildTypeLocator(id = id))

    override fun deleteBuildType(buildType: BuildTypeLocator) =
        client.deleteBuildType(buildType)

    fun deleteBuildType(id: String) =
        deleteBuildType(BuildTypeLocator(id = id))

    override fun getBuildTypes() =
        client.getBuildTypes()

    override fun getBuildTypes(project: ProjectLocator) =
        client.getBuildTypes(project)

    fun getBuildTypes(projectId: String) =
        getBuildTypes(ProjectLocator(id = projectId))

    override fun addBuildTypeFeature(buildType: BuildTypeLocator, feature: TeamcityLinkFeature) =
        client.addBuildTypeFeature(buildType, feature)

    fun addBuildTypeFeature(buildTypeId: String, feature: TeamcityLinkFeature) =
        addBuildTypeFeature(BuildTypeLocator(id = buildTypeId), feature)

    override fun getBuildTypeFeatures(buildType: BuildTypeLocator) =
        client.getBuildTypeFeatures(buildType)

    fun getBuildTypeFeatures(buildTypeId: String) =
        getBuildTypeFeatures(BuildTypeLocator(id = buildTypeId))

    override fun getBuildTypeFeature(buildType: BuildTypeLocator, feature: String) =
        client.getBuildTypeFeature(buildType, feature)

    fun getBuildTypeFeature(buildTypeId: String, feature: String) =
        getBuildTypeFeature(BuildTypeLocator(id = buildTypeId), feature)

    override fun updateBuildTypeFeatureParameter(
        buildType: BuildTypeLocator,
        feature: String,
        parameter: String,
        newValue: String
    ) = client.updateBuildTypeFeatureParameter(buildType, feature, parameter, newValue)

    fun updateBuildTypeFeatureParameter(
        buildTypeId: String,
        feature: String,
        parameter: String,
        newValue: String
    ) = updateBuildTypeFeatureParameter(BuildTypeLocator(id = buildTypeId), feature, parameter, newValue)

    override fun getBuildTypeFeatureParameter(
        buildType: BuildTypeLocator,
        feature: String,
        parameter: String
    ) = client.getBuildTypeFeatureParameter(buildType, feature, parameter)

    fun getBuildTypeFeatureParameter(
        buildTypeId: String,
        feature: String,
        parameter: String
    ) = getBuildTypeFeatureParameter(BuildTypeLocator(id = buildTypeId), feature, parameter)

    override fun setBuildCounter(buildType: BuildTypeLocator, newValue: String) =
        client.setBuildCounter(buildType, newValue)

    fun setBuildCounter(buildTypeId: String, newValue: String) =
        setBuildCounter(BuildTypeLocator(id = buildTypeId), newValue)

    override fun createSnapshotDependency(buildType: BuildTypeLocator, dependency: TeamcitySnapshotDependency) =
        client.createSnapshotDependency(buildType, dependency)

    fun createSnapshotDependency(buildTypeId: String, dependency: TeamcitySnapshotDependency) =
        createSnapshotDependency(BuildTypeLocator(id = buildTypeId), dependency)

    override fun deleteSnapshotDependency(buildType: BuildTypeLocator, dependency: String) =
        client.deleteSnapshotDependency(buildType, dependency)

    fun deleteSnapshotDependency(buildTypeId: String, dependency: String) =
        deleteSnapshotDependency(BuildTypeLocator(id = buildTypeId), dependency)

    override fun getSnapshotDependencies(buildType: BuildTypeLocator): TeamcitySnapshotDependencies =
        client.getSnapshotDependencies(buildType)

    fun getSnapshotDependencies(buildTypeId: String): TeamcitySnapshotDependencies =
        getSnapshotDependencies(BuildTypeLocator(id = buildTypeId))

    override fun disableBuildStep(buildType: BuildTypeLocator, step: String, newValue: Boolean) =
        client.disableBuildStep(buildType, step, newValue)

    fun disableBuildStep(buildTypeId: String, step: String, newValue: Boolean) =
        disableBuildStep(BuildTypeLocator(id = buildTypeId), step, newValue)

    override fun createBuildStep(buildType: BuildTypeLocator, step: TeamcityStep) =
        client.createBuildStep(buildType, step)

    fun createBuildStep(buildTypeId: String, step: TeamcityStep) =
        createBuildStep(BuildTypeLocator(id = buildTypeId), step)

    override fun getBuildSteps(buildType: BuildTypeLocator) = client.getBuildSteps(buildType)

    fun getBuildSteps(buildTypeId: String) = getBuildSteps(BuildTypeLocator(id = buildTypeId))

    override fun createBuildTypeVcsRootEntry(buildType: BuildTypeLocator, vcsRootEntry: TeamcityCreateVcsRootEntry) =
        client.createBuildTypeVcsRootEntry(buildType, vcsRootEntry)

    fun createBuildTypeVcsRootEntry(buildTypeId: String, vcsRootEntry: TeamcityCreateVcsRootEntry) =
        createBuildTypeVcsRootEntry(BuildTypeLocator(id = buildTypeId), vcsRootEntry)

    override fun deleteBuildTypeVcsRootEntry(buildType: BuildTypeLocator, vcsRootEntryId: String) =
        client.deleteBuildTypeVcsRootEntry(buildType, vcsRootEntryId)

    fun deleteBuildTypeVcsRootEntry(buildTypeId: String, vcsRootEntryId: String) =
        deleteBuildTypeVcsRootEntry(BuildTypeLocator(id = buildTypeId), vcsRootEntryId)

    override fun getBuildTypeVcsRootEntries(buildType: BuildTypeLocator): TeamcityVcsRootEntries =
        client.getBuildTypeVcsRootEntries(buildType)

    fun getBuildTypeVcsRootEntries(buildTypeId: String): TeamcityVcsRootEntries =
        getBuildTypeVcsRootEntries(BuildTypeLocator(id = buildTypeId))

    override fun getBuildTypeVcsRootEntry(buildType: BuildTypeLocator, vcsRootEntryId: String): TeamcityVcsRootEntry =
        client.getBuildTypeVcsRootEntry(buildType, vcsRootEntryId)

    fun getBuildTypeVcsRootEntry(buildTypeId: String, vcsRootEntryId: String): TeamcityVcsRootEntry =
        getBuildTypeVcsRootEntry(BuildTypeLocator(id = buildTypeId), vcsRootEntryId)

    override fun updateBuildTypeVcsRootEntryCheckoutRules(
        buildType: BuildTypeLocator,
        vcsRootEntryId: String,
        checkoutRules: String
    ) = client.updateBuildTypeVcsRootEntryCheckoutRules(buildType, vcsRootEntryId, checkoutRules)

    fun updateBuildTypeVcsRootEntryCheckoutRules(
        buildTypeId: String,
        vcsRootEntryId: String,
        checkoutRules: String
    ) = updateBuildTypeVcsRootEntryCheckoutRules(BuildTypeLocator(id = buildTypeId), vcsRootEntryId, checkoutRules)

    override fun createVcsRoot(vcsRoot: TeamcityCreateVcsRoot) =
        client.createVcsRoot(vcsRoot)

    override fun getVcsRoot(vcsRoot: VcsRootLocator): TeamcityVcsRoot =
        client.getVcsRoot(vcsRoot)

    fun getVcsRoot(vcsRootId: String): TeamcityVcsRoot =
        getVcsRoot(VcsRootLocator(id = vcsRootId))

    override fun updateVcsRootProperty(vcsRootId: String, propertyName: String, newValue: String) =
        client.updateVcsRootProperty(vcsRootId, propertyName, newValue)

    override fun getVcsRootProperty(vcsRootId: String, propertyName: String) =
        client.getVcsRootProperty(vcsRootId, propertyName)

    override fun getBuildTypeTemplate(buildType: BuildTypeLocator) =
        client.getBuildTypeTemplate(buildType)

    fun getBuildTypeTemplate(buildTypeId: String) =
        getBuildTypeTemplate(BuildTypeLocator(id = buildTypeId))

    override fun attachTemplateToBuildType(buildType: BuildTypeLocator, template: String) =
        client.attachTemplateToBuildType(buildType, template)

    fun attachTemplateToBuildType(buildTypeId: String, template: String) =
        attachTemplateToBuildType(BuildTypeLocator(id = buildTypeId), template)

    override fun detachTemplatesFromBuildType(buildType: BuildTypeLocator) =
        client.detachTemplatesFromBuildType(buildType)

    fun detachTemplatesFromBuildType(buildTypeId: String) =
        detachTemplatesFromBuildType(BuildTypeLocator(id = buildTypeId))

    override fun createParameter(type: ConfigurationType, id: String, parameter: TeamcityProperty) =
        client.createParameter(type, id, parameter)

    override fun createParameter(type: ConfigurationType, id: String, name: String, value: String) =
        client.createParameter(type, id, name, value)

    override fun setParameter(type: ConfigurationType, id: String, name: String, value: String) =
        client.setParameter(type, id, name, value)

    override fun getParameter(type: ConfigurationType, id: String, name: String) =
        client.getParameter(type, id, name)

    override fun deleteParameter(type: ConfigurationType, id: String, name: String) =
        client.deleteParameter(type, id, name)

    override fun getVcsRootInstances(locator: VcsRootInstanceLocator) =
        client.getVcsRootInstances(locator)

    override fun getVcsRoots(locator: VcsRootLocator): TeamcityVcsRoots =
        client.getVcsRoots(locator)
}
