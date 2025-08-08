package org.octopusden.octopus.infrastructure.teamcity.client

import com.fasterxml.jackson.annotation.JsonValue
import feign.Body
import feign.Headers
import feign.Param
import feign.RequestLine
import feign.form.FormData
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityAgentRequirement
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityAgentRequirements
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityBuildTypes
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRootEntry
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityFeature
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityFeatures
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkFeature
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProjects
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperty
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityQueuedBuild
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityServer
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependencies
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependency
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityStep
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySteps
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootEntries
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootEntry
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootInstances
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRoots
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.AgentRequirementLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildTypeLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.ProjectLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootInstanceLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootLocator
import org.octopusden.octopus.infrastructure.teamcity.client.TeamcityLocatorExpander as Locator

private const val REST: String = "/app/rest/2018.1"

interface TeamcityClient {
    @RequestLine("GET $REST/server")
    @Headers("Accept: application/json")
    fun getServer(): TeamcityServer

    @RequestLine("POST $REST/projects")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun createProject(dto: TeamcityCreateProject): TeamcityProject

    @RequestLine("DELETE $REST/projects/{locator}")
    fun deleteProject(@Param("locator", expander = Locator::class) locator: ProjectLocator)

    @RequestLine("GET $REST/projects/{locator}")
    @Headers("Accept: application/json")
    fun getProject(@Param("locator", expander = Locator::class) locator: ProjectLocator): TeamcityProject

    @RequestLine("GET $REST/projects?locator={locator}")
    @Headers("Accept: application/json")
    fun getProjects(@Param("locator", expander = Locator::class) locator: ProjectLocator): TeamcityProjects

    @RequestLine("POST $REST/buildTypes")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun createBuildType(dto: TeamcityCreateBuildType): TeamcityBuildType

    @RequestLine("POST $REST/projects/{locator}/buildTypes")
    @Headers("Content-Type: text/plain", "Accept: application/json")
    @Body("{name}")
    fun createBuildType(
        @Param("locator", expander = Locator::class) project: ProjectLocator, @Param("name") name: String
    ): TeamcityBuildType

    @RequestLine("DELETE $REST/buildTypes/{locator}")
    fun deleteBuildType(@Param("locator", expander = Locator::class) buildType: BuildTypeLocator)

    @RequestLine("GET $REST/buildTypes/{locator}")
    @Headers("Accept: application/json")
    fun getBuildType(@Param("locator", expander = Locator::class) buildType: BuildTypeLocator): TeamcityBuildType

    @RequestLine("GET $REST/buildTypes")
    @Headers("Accept: application/json")
    fun getBuildTypes(): TeamcityBuildTypes

    /**
     * Get all build types with the specified fields.
     * Example: `fields=id,name,project(id,name),vcs-root-entries(id,name)`
     */
    @RequestLine("GET $REST/buildTypes?fields={fields}")
    @Headers("Accept: application/json")
    fun getBuildTypesWithFields(
        @Param("fields") fields: String
    ): TeamcityBuildTypes

    @RequestLine("GET $REST/projects/{locator}/buildTypes")
    @Headers("Accept: application/json")
    fun getBuildTypes(@Param("locator", expander = Locator::class) project: ProjectLocator): TeamcityBuildTypes

    /**
     * Get all build types of the specified project with the specified fields.
     * Example: `fields=buildType(id,name,template(id,name))`
     */
    @RequestLine("GET $REST/projects/{locator}/buildTypes?fields={fields}")
    @Headers("Accept: application/json")
    fun getBuildTypesProjectWithFields(@Param("locator", expander = Locator::class) project: ProjectLocator, @Param("fields") fields: String): TeamcityBuildTypes

    /**
     * Add an agent requirement to the matching build configuration.
     *
     * Note, this is equivalent to the other `addAgentRequirementToBuildType` that receives the query parameters as a map,
     * but this one also exposes the Http response headers
     * @param buildType  (required)
     * @param body  (optional)
     *
     * @return TeamcityAgentRequirement
     */
    @RequestLine("POST $REST/buildTypes/{locator}/agent-requirements")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun addAgentRequirementToBuildType(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        body: TeamcityAgentRequirement
    ): TeamcityAgentRequirement

    /**
     * Remove an agent requirement of the matching build configuration.
     *
     * @param buildType  (required)
     * @param agentRequirementLocator  (required)
     */
    @RequestLine("DELETE /app/rest/buildTypes/{locator}/agent-requirements/{agentRequirementLocator}")
    @Headers("Accept: application/json")
    fun deleteAgentRequirement(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("agentRequirementLocator", expander = Locator::class) agentRequirementLocator: AgentRequirementLocator
    )

    /**
     * Get all agent requirements of the matching build configuration.
     *
     * @param buildType  (required)
     * @return TeamcityAgentRequirements
     */
    @RequestLine("GET $REST/buildTypes/{locator}/agent-requirements")
    @Headers("Accept: application/json")
    fun getAgentRequirements(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator
    ): TeamcityAgentRequirements

    @RequestLine("POST $REST/buildTypes/{locator}/features")
    @Headers("Content-Type: application/json")
    fun addBuildTypeFeature(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator, feature: TeamcityLinkFeature
    )

    @RequestLine("GET $REST/buildTypes/{locator}/features")
    @Headers("Accept: application/json")
    fun getBuildTypeFeatures(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator
    ): TeamcityFeatures

    @RequestLine("GET $REST/buildTypes/{locator}/features/{feature}")
    @Headers("Accept: application/json")
    fun getBuildTypeFeature(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator, @Param("feature") feature: String
    ): TeamcityFeature

    @RequestLine("PUT $REST/buildTypes/{locator}/features/{feature}/parameters/{parameter}")
    @Headers("Content-Type: text/plain")
    @Body("{newValue}")
    fun updateBuildTypeFeatureParameter(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("feature") feature: String,
        @Param("parameter") parameter: String,
        @Param("newValue") newValue: String
    )

    @RequestLine("GET $REST/buildTypes/{locator}/features/{feature}/parameters/{parameter}")
    @Headers("Accept: text/plain")
    fun getBuildTypeFeatureParameter(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("feature") feature: String,
        @Param("parameter") parameter: String
    ): String

    @RequestLine("PUT $REST/buildTypes/{locator}/settings/buildNumberCounter")
    @Headers("Content-Type: text/plain")
    @Body("{newValue}")
    fun setBuildCounter(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator, @Param("newValue") newValue: String
    )

    @RequestLine("POST $REST/buildTypes/{locator}/snapshot-dependencies")
    @Headers("Content-Type: application/json")
    fun createSnapshotDependency(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator, dependency: TeamcitySnapshotDependency
    )

    @RequestLine("DELETE $REST/buildTypes/{locator}/snapshot-dependencies/{dependency}")
    @Headers("Accept: application/json")
    fun deleteSnapshotDependency(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("dependency") dependency: String
    )

    @RequestLine("GET $REST/buildTypes/{locator}/snapshot-dependencies")
    @Headers("Accept: application/json")
    fun getSnapshotDependencies(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator
    ): TeamcitySnapshotDependencies

    @RequestLine("PUT $REST/buildTypes/{locator}/steps/{step}/disabled")
    @Headers("Content-Type: text/plain")
    @Body("{newValue}")
    fun disableBuildStep(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("step") step: String,
        @Param("newValue") newValue: Boolean
    )

    @RequestLine("POST $REST/buildTypes/{locator}/steps")
    @Headers("Content-Type: application/json")
    fun createBuildStep(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator, step: TeamcityStep
    )

    @RequestLine("GET $REST/buildTypes/{locator}/steps")
    @Headers("Accept: application/json")
    fun getBuildSteps(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator
    ): TeamcitySteps

    @RequestLine("POST $REST/buildTypes/{locator}/vcs-root-entries")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun createBuildTypeVcsRootEntry(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        vcsRootEntry: TeamcityCreateVcsRootEntry
    ): TeamcityVcsRootEntry

    @RequestLine("DELETE $REST/buildTypes/{locator}/vcs-root-entries/{vcsRootEntryId}")
    @Headers("Accept: application/json")
    fun deleteBuildTypeVcsRootEntry(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("vcsRootEntryId") vcsRootEntryId: String
    )

    @RequestLine("GET $REST/buildTypes/{locator}/vcs-root-entries")
    @Headers("Accept: application/json")
    fun getBuildTypeVcsRootEntries(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator
    ): TeamcityVcsRootEntries

    @RequestLine("GET $REST/buildTypes/{locator}/vcs-root-entries/{vcsRootEntryId}")
    @Headers("Accept: application/json")
    fun getBuildTypeVcsRootEntry(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("vcsRootEntryId") vcsRootEntryId: String
    ): TeamcityVcsRootEntry

    @RequestLine("PUT $REST/buildTypes/{locator}/vcs-root-entries/{vcsRootEntryId}/checkout-rules")
    @Headers("Content-Type: text/plain")
    @Body("{checkoutRules}")
    fun updateBuildTypeVcsRootEntryCheckoutRules(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("vcsRootEntryId") vcsRootEntryId: String,
        @Param("checkoutRules") checkoutRules: String
    )

    @RequestLine("POST $REST/vcs-roots")
    @Headers("Accept: application/json", "Content-Type: application/json")
    fun createVcsRoot(vcsRoot: TeamcityCreateVcsRoot): TeamcityVcsRoot

    @RequestLine("GET $REST/vcs-roots/{locator}")
    @Headers("Accept: application/json")
    fun getVcsRoot(@Param("locator", expander = Locator::class) vcsRoot: VcsRootLocator): TeamcityVcsRoot

    @RequestLine("PUT $REST/vcs-roots/{vcsRootId}/properties/{propertyName}")
    @Headers("Content-Type: text/plain")
    @Body("{newValue}")
    fun updateVcsRootProperty(
        @Param("vcsRootId") vcsRootId: String,
        @Param("propertyName") propertyName: String,
        @Param("newValue") newValue: String
    )

    @RequestLine("GET $REST/vcs-roots/{vcsRootId}/properties/{propertyName}")
    @Headers("Accept: text/plain")
    fun getVcsRootProperty(
        @Param("vcsRootId") vcsRootId: String,
        @Param("propertyName") propertyName: String,
    ): String

    @RequestLine("GET $REST/buildTypes/{locator}/template")
    @Headers("Accept: text/plain")
    fun getBuildTypeTemplate(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator
    ): String

    @RequestLine("PUT $REST/buildTypes/{locator}/template")
    @Headers("Content-Type: text/plain")
    @Body("{template}")
    fun attachTemplateToBuildType(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator, @Param("template") template: String
    )

    @RequestLine("DELETE $REST/buildTypes/{locator}/templates")
    fun detachTemplatesFromBuildType(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
    )

    @RequestLine("POST $REST/{type}/{id}/parameters")
    @Headers("Content-Type: application/json")
    fun createParameter(
        @Param("type", expander = ConfigurationType.ConfigurationTypeExpander::class) type: ConfigurationType,
        @Param("id") id: String,
        parameter: TeamcityProperty
    )

    @RequestLine("POST $REST/{type}/{id}/parameters")
    @Headers("Content-Type: application/xml")
    @Body("<property name=\"{name}\" value=\"{value}\"/>")
    fun createParameter(
        @Param("type", expander = ConfigurationType.ConfigurationTypeExpander::class) type: ConfigurationType,
        @Param("id") id: String,
        @Param("name") name: String,
        @Param("value") value: String = ""
    )

    @RequestLine("PUT $REST/{type}/{id}/parameters/{name}")
    @Headers("Content-Type: text/plain")
    @Body("{value}")
    fun setParameter(
        @Param("type", expander = ConfigurationType.ConfigurationTypeExpander::class) type: ConfigurationType,
        @Param("id") id: String,
        @Param("name") name: String,
        @Param("value") value: String
    )

    @RequestLine("GET $REST/{type}/{id}/parameters/{name}")
    @Headers("Accept: text/plain")
    fun getParameter(
        @Param("type", expander = ConfigurationType.ConfigurationTypeExpander::class) type: ConfigurationType,
        @Param("id") id: String,
        @Param("name") name: String
    ): String

    @RequestLine("DELETE $REST/{type}/{id}/parameters/{name}")
    fun deleteParameter(
        @Param("type", expander = ConfigurationType.ConfigurationTypeExpander::class) type: ConfigurationType,
        @Param("id") id: String,
        @Param("name") name: String
    )

    @RequestLine("GET $REST/vcs-root-instances?locator={locator}")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getVcsRootInstances(
        @Param("locator", expander = Locator::class) locator: VcsRootInstanceLocator
    ): TeamcityVcsRootInstances

    @RequestLine("GET $REST/vcs-roots?locator={locator}")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getVcsRoots(
        @Param("locator", expander = Locator::class) locator: VcsRootLocator
    ): TeamcityVcsRoots

    @RequestLine("POST /plugins/metarunner/upload.html")
    @Headers("Content-Type: multipart/form-data")
    fun uploadMetarunner(
        @Param(value = "fileName") fileName: String,
        @Param(value = "file:fileToUpload") file: FormData,
        @Param(value = "action") action: String,
        @Param(value = "projectId") projectId: String
    )

    @RequestLine("POST /plugins/recipes/upload.html")
    @Headers("Content-Type: multipart/form-data")
    fun uploadRecipe(
        @Param(value = "fileName") fileName: String,
        @Param(value = "file:fileToUpload") file: FormData,
        @Param(value = "action") action: String,
        @Param(value = "projectId") projectId: String
    )

    @RequestLine("POST $REST/buildQueue")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun queueBuild(build: TeamcityQueuedBuild): TeamcityQueuedBuild

    @RequestLine("GET $REST/projects?locator={locator}&fields={fields}")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getProjectsWithLocatorAndFields(
        @Param("locator", expander = Locator::class) locator: ProjectLocator,
        @Param("fields") fields: String
    ): TeamcityProjects

    @RequestLine("GET $REST/buildTypes?locator=vcsRootInstance({locator})&fields={fields}")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getBuildTypesWithLocatorAndFields(
        @Param("locator", expander = Locator::class) locator: VcsRootInstanceLocator,
        @Param("fields") fields: String
    ): TeamcityBuildTypes
}

enum class ConfigurationType(
    @get:JsonValue val type: String
) {
    PROJECT("projects"), BUILD_TYPE("buildTypes");

    class ConfigurationTypeExpander : Param.Expander {
        override fun expand(value: Any?) = when (value) {
            is ConfigurationType -> value.type
            else -> throw Exception("Unknown class ${value}")
        }
    }
}

fun TeamcityClient.deleteProject(id: String) = deleteProject(ProjectLocator(id = id))

fun TeamcityClient.getProject(id: String) = getProject(ProjectLocator(id = id))

fun TeamcityClient.createBuildType(projectId: String, name: String) =
    createBuildType(ProjectLocator(id = projectId), name)

fun TeamcityClient.getBuildType(id: String) = getBuildType(BuildTypeLocator(id = id))

fun TeamcityClient.deleteBuildType(id: String) = deleteBuildType(BuildTypeLocator(id = id))

fun TeamcityClient.getBuildTypes(projectId: String) = getBuildTypes(ProjectLocator(id = projectId))

fun TeamcityClient.addAgentRequirementToBuildType(buildTypeId: String, requirement: TeamcityAgentRequirement) =
    addAgentRequirementToBuildType(BuildTypeLocator(id = buildTypeId), requirement)

fun TeamcityClient.deleteAgentRequirement(buildTypeId: String, agentRequirementLocator: String) =
    deleteAgentRequirement(BuildTypeLocator(id = buildTypeId), AgentRequirementLocator(id = agentRequirementLocator))

fun TeamcityClient.getAgentRequirements(buildTypeId: String) =
    getAgentRequirements(BuildTypeLocator(id = buildTypeId))

fun TeamcityClient.addBuildTypeFeature(buildTypeId: String, feature: TeamcityLinkFeature) =
    addBuildTypeFeature(BuildTypeLocator(id = buildTypeId), feature)

fun TeamcityClient.getBuildTypeFeatures(buildTypeId: String) = getBuildTypeFeatures(BuildTypeLocator(id = buildTypeId))

fun TeamcityClient.getBuildTypeFeature(buildTypeId: String, feature: String) =
    getBuildTypeFeature(BuildTypeLocator(id = buildTypeId), feature)

fun TeamcityClient.updateBuildTypeFeatureParameter(
    buildTypeId: String, feature: String, parameter: String, newValue: String
) = updateBuildTypeFeatureParameter(BuildTypeLocator(id = buildTypeId), feature, parameter, newValue)

fun TeamcityClient.getBuildTypeFeatureParameter(buildTypeId: String, feature: String, parameter: String) =
    getBuildTypeFeatureParameter(BuildTypeLocator(id = buildTypeId), feature, parameter)

fun TeamcityClient.setBuildCounter(buildTypeId: String, newValue: String) =
    setBuildCounter(BuildTypeLocator(id = buildTypeId), newValue)

fun TeamcityClient.createSnapshotDependency(buildTypeId: String, dependency: TeamcitySnapshotDependency) =
    createSnapshotDependency(BuildTypeLocator(id = buildTypeId), dependency)

fun TeamcityClient.deleteSnapshotDependency(buildTypeId: String, dependency: String) =
    deleteSnapshotDependency(BuildTypeLocator(id = buildTypeId), dependency)

fun TeamcityClient.getSnapshotDependencies(buildTypeId: String) =
    getSnapshotDependencies(BuildTypeLocator(id = buildTypeId))

fun TeamcityClient.disableBuildStep(buildTypeId: String, step: String, newValue: Boolean) =
    disableBuildStep(BuildTypeLocator(id = buildTypeId), step, newValue)

fun TeamcityClient.createBuildStep(buildTypeId: String, step: TeamcityStep) =
    createBuildStep(BuildTypeLocator(id = buildTypeId), step)

fun TeamcityClient.getBuildSteps(buildTypeId: String) = getBuildSteps(BuildTypeLocator(id = buildTypeId))

fun TeamcityClient.createBuildTypeVcsRootEntry(buildTypeId: String, vcsRootEntry: TeamcityCreateVcsRootEntry) =
    createBuildTypeVcsRootEntry(BuildTypeLocator(id = buildTypeId), vcsRootEntry)

fun TeamcityClient.deleteBuildTypeVcsRootEntry(buildTypeId: String, vcsRootEntryId: String) =
    deleteBuildTypeVcsRootEntry(BuildTypeLocator(id = buildTypeId), vcsRootEntryId)

fun TeamcityClient.getBuildTypeVcsRootEntries(buildTypeId: String) =
    getBuildTypeVcsRootEntries(BuildTypeLocator(id = buildTypeId))

fun TeamcityClient.getBuildTypeVcsRootEntry(buildTypeId: String, vcsRootEntryId: String) =
    getBuildTypeVcsRootEntry(BuildTypeLocator(id = buildTypeId), vcsRootEntryId)

fun TeamcityClient.updateBuildTypeVcsRootEntryCheckoutRules(
    buildTypeId: String, vcsRootEntryId: String, checkoutRules: String
) = updateBuildTypeVcsRootEntryCheckoutRules(BuildTypeLocator(id = buildTypeId), vcsRootEntryId, checkoutRules)

fun TeamcityClient.getVcsRoot(vcsRootId: String) = getVcsRoot(VcsRootLocator(id = vcsRootId))

fun TeamcityClient.getBuildTypeTemplate(buildTypeId: String) = getBuildTypeTemplate(BuildTypeLocator(id = buildTypeId))

fun TeamcityClient.attachTemplateToBuildType(buildTypeId: String, template: String) =
    attachTemplateToBuildType(BuildTypeLocator(id = buildTypeId), template)

fun TeamcityClient.detachTemplatesFromBuildType(buildTypeId: String) =
    detachTemplatesFromBuildType(BuildTypeLocator(id = buildTypeId))

fun TeamcityClient.uploadMetarunner(projectId: String, fileName: String, fileContent: ByteArray) {
    val majorVersion = getServer().version.substringBefore(".").toInt()
    if (majorVersion < 2025) {
        uploadMetarunner(fileName, FormData("text/xml", fileName, fileContent), "uploadMetarunner", projectId)
    } else {
        uploadRecipe(fileName, FormData("text/xml", fileName, fileContent), "uploadRecipe", projectId)
    }
}
