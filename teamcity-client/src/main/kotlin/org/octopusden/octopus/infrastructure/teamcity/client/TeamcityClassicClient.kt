package org.octopusden.octopus.infrastructure.teamcity.client

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.Logger
import feign.RequestInterceptor
import feign.form.FormData
import feign.form.FormEncoder
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityAgentRequirement
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityBuildTypes
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRootEntry
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkFeature
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProjects
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperty
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityQueuedBuild
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependencies
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependency
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityStep
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.AgentRequirementLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildTypeLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.ProjectLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootInstanceLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootLocator

class TeamcityClassicClient(
    apiParametersProvider: ClientParametersProvider, mapper: ObjectMapper = getMapper()
) : TeamcityClient {
    private val client: TeamcityClient = createClient(
        apiParametersProvider.getApiUrl(), apiParametersProvider.getAuth().getInterceptor(), mapper
    )

    override fun getServer() = client.getServer()

    override fun createProject(dto: TeamcityCreateProject) = client.createProject(dto)

    override fun deleteProject(locator: ProjectLocator) = client.deleteProject(locator)

    override fun getProject(locator: ProjectLocator) = client.getProject(locator)

    override fun getProjects(locator: ProjectLocator) = client.getProjects(locator)

    override fun createBuildType(dto: TeamcityCreateBuildType) = client.createBuildType(dto)

    override fun createBuildType(project: ProjectLocator, name: String) = client.createBuildType(project, name)

    override fun getBuildType(buildType: BuildTypeLocator) = client.getBuildType(buildType)

    override fun deleteBuildType(buildType: BuildTypeLocator) = client.deleteBuildType(buildType)

    override fun getBuildTypes() = client.getBuildTypes()

    override fun getBuildTypesWithFields(fields: String) = client.getBuildTypesWithFields(fields)

    override fun getBuildTypes(project: ProjectLocator) = client.getBuildTypes(project)

    override fun getBuildTypesProjectWithFields(project: ProjectLocator,fields: String) = client.getBuildTypesProjectWithFields(project, fields)

    override fun deleteAgentRequirement(buildType: BuildTypeLocator, agentRequirementLocator: AgentRequirementLocator) =
        client.deleteAgentRequirement(buildType, agentRequirementLocator)

    override fun addAgentRequirementToBuildType(
        buildType: BuildTypeLocator,
        body: TeamcityAgentRequirement
    ): TeamcityAgentRequirement = client.addAgentRequirementToBuildType(buildType, body)

    override fun getAgentRequirements(buildType: BuildTypeLocator) = client.getAgentRequirements(
        buildType
    )

    override fun addBuildTypeFeature(buildType: BuildTypeLocator, feature: TeamcityLinkFeature) =
        client.addBuildTypeFeature(buildType, feature)

    override fun getBuildTypeFeatures(buildType: BuildTypeLocator) = client.getBuildTypeFeatures(buildType)

    override fun getBuildTypeFeature(buildType: BuildTypeLocator, feature: String) =
        client.getBuildTypeFeature(buildType, feature)

    override fun updateBuildTypeFeatureParameter(
        buildType: BuildTypeLocator, feature: String, parameter: String, newValue: String
    ) = client.updateBuildTypeFeatureParameter(buildType, feature, parameter, newValue)

    override fun getBuildTypeFeatureParameter(
        buildType: BuildTypeLocator, feature: String, parameter: String
    ) = client.getBuildTypeFeatureParameter(buildType, feature, parameter)

    override fun setBuildCounter(buildType: BuildTypeLocator, newValue: String) =
        client.setBuildCounter(buildType, newValue)

    override fun createSnapshotDependency(buildType: BuildTypeLocator, dependency: TeamcitySnapshotDependency) =
        client.createSnapshotDependency(buildType, dependency)

    override fun deleteSnapshotDependency(buildType: BuildTypeLocator, dependency: String) =
        client.deleteSnapshotDependency(buildType, dependency)

    override fun getSnapshotDependencies(buildType: BuildTypeLocator): TeamcitySnapshotDependencies =
        client.getSnapshotDependencies(buildType)

    override fun disableBuildStep(buildType: BuildTypeLocator, step: String, newValue: Boolean) =
        client.disableBuildStep(buildType, step, newValue)

    override fun createBuildStep(buildType: BuildTypeLocator, step: TeamcityStep) =
        client.createBuildStep(buildType, step)

    override fun getBuildSteps(buildType: BuildTypeLocator) = client.getBuildSteps(buildType)

    override fun createBuildTypeVcsRootEntry(buildType: BuildTypeLocator, vcsRootEntry: TeamcityCreateVcsRootEntry) =
        client.createBuildTypeVcsRootEntry(buildType, vcsRootEntry)

    override fun deleteBuildTypeVcsRootEntry(buildType: BuildTypeLocator, vcsRootEntryId: String) =
        client.deleteBuildTypeVcsRootEntry(buildType, vcsRootEntryId)

    override fun getBuildTypeVcsRootEntries(buildType: BuildTypeLocator) = client.getBuildTypeVcsRootEntries(buildType)

    override fun getBuildTypeVcsRootEntry(buildType: BuildTypeLocator, vcsRootEntryId: String) =
        client.getBuildTypeVcsRootEntry(buildType, vcsRootEntryId)

    override fun updateBuildTypeVcsRootEntryCheckoutRules(
        buildType: BuildTypeLocator, vcsRootEntryId: String, checkoutRules: String
    ) = client.updateBuildTypeVcsRootEntryCheckoutRules(buildType, vcsRootEntryId, checkoutRules)

    override fun createVcsRoot(vcsRoot: TeamcityCreateVcsRoot) = client.createVcsRoot(vcsRoot)

    override fun getVcsRoot(vcsRoot: VcsRootLocator): TeamcityVcsRoot = client.getVcsRoot(vcsRoot)

    override fun updateVcsRootProperty(vcsRootId: String, propertyName: String, newValue: String) =
        client.updateVcsRootProperty(vcsRootId, propertyName, newValue)

    override fun getVcsRootProperty(vcsRootId: String, propertyName: String) =
        client.getVcsRootProperty(vcsRootId, propertyName)

    override fun getBuildTypeTemplate(buildType: BuildTypeLocator) = client.getBuildTypeTemplate(buildType)

    override fun attachTemplateToBuildType(buildType: BuildTypeLocator, template: String) =
        client.attachTemplateToBuildType(buildType, template)

    override fun detachTemplatesFromBuildType(buildType: BuildTypeLocator) =
        client.detachTemplatesFromBuildType(buildType)

    override fun createParameter(type: ConfigurationType, id: String, parameter: TeamcityProperty) =
        client.createParameter(type, id, parameter)

    override fun createParameter(type: ConfigurationType, id: String, name: String, value: String) =
        client.createParameter(type, id, name, value)

    override fun setParameter(type: ConfigurationType, id: String, name: String, value: String) =
        client.setParameter(type, id, name, value)

    override fun getParameter(type: ConfigurationType, id: String, name: String) = client.getParameter(type, id, name)

    override fun deleteParameter(type: ConfigurationType, id: String, name: String) =
        client.deleteParameter(type, id, name)

    override fun getVcsRootInstances(locator: VcsRootInstanceLocator) = client.getVcsRootInstances(locator)

    override fun getVcsRoots(locator: VcsRootLocator) = client.getVcsRoots(locator)

    override fun uploadMetarunner(fileName: String, file: FormData, action: String, projectId: String) =
        client.uploadMetarunner(fileName, file, action, projectId)

    override fun uploadRecipe(fileName: String, file: FormData, action: String, projectId: String) =
        client.uploadRecipe(fileName, file, action, projectId)

    override fun queueBuild(build: TeamcityQueuedBuild): TeamcityQueuedBuild = client.queueBuild(build)

    override fun getProjectsWithLocatorAndFields(locator: ProjectLocator, fields: String): TeamcityProjects =
        client.getProjectsWithLocatorAndFields(locator, fields)

    override fun getBuildTypesWithVcsRootInstanceLocatorAndFields(locator: VcsRootInstanceLocator, fields: String): TeamcityBuildTypes =
        client.getBuildTypesWithVcsRootInstanceLocatorAndFields(locator, fields)

    companion object {
        private fun getMapper() = jacksonObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }

        private fun createClient(apiUrl: String, interceptor: RequestInterceptor, objectMapper: ObjectMapper) =
            Feign.builder().requestInterceptor { requestTemplate -> requestTemplate?.header("Origin", apiUrl) }
                .client(ApacheHttpClient()).encoder(FormEncoder(JacksonEncoder(objectMapper)))
                .decoder(TeamcityClientDecoder(objectMapper)).requestInterceptor(interceptor)
                .logger(Slf4jLogger(TeamcityClient::class.java)).logLevel(Logger.Level.FULL)
                .target(TeamcityClient::class.java, apiUrl)
    }
}
