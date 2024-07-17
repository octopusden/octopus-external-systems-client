package org.octopusden.octopus.infrastructure.teamcity.client

import feign.Body
import feign.Headers
import feign.Param
import feign.RequestLine
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
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependencies
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependency
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityStep
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySteps
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootEntries
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootEntry
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootInstances
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRoots
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.ProjectLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootInstanceLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootLocator

private const val API_VERSION: String = "2018.1"
private const val REST: String = "/app/rest/$API_VERSION"

interface TeamcityClient {

    @RequestLine("POST $REST/projects")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun createProject(dto: TeamcityCreateProject): TeamcityProject

    @RequestLine("DELETE $REST/projects/{project}")
    fun deleteProject(@Param("project") project: String)

    @RequestLine("GET $REST/projects/{project}")
    @Headers("Accept: application/json")
    fun getProject(@Param("project") project: String): TeamcityProject

    @RequestLine("GET $REST/projects?locator={locator}")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getProjectsByLocator(@Param("locator") locator: ProjectLocator): TeamcityProjects

    @RequestLine("POST $REST/buildTypes")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun createBuildType(dto: TeamcityCreateBuildType): TeamcityBuildType

    @RequestLine("POST $REST/projects/{project}/buildTypes")
    @Headers("Content-Type: text/plain", "Accept: application/json")
    @Body("{name}")
    fun createBuildType(@Param("project") project: String, @Param("name") buildTypeName: String): TeamcityBuildType

    @RequestLine("DELETE $REST/buildTypes/{buildType}")
    fun deleteBuildType(@Param("buildType") buildType: String)

    @RequestLine("GET $REST/buildTypes/{buildType}")
    @Headers("Accept: application/json")
    fun getBuildType(@Param("buildType") buildType: String): TeamcityBuildType

    @RequestLine("GET $REST/buildTypes")
    @Headers("Accept: application/json")
    fun getBuildTypes(): TeamcityBuildTypes

    @RequestLine("GET $REST/projects/{project}/buildTypes")
    @Headers("Accept: application/json")
    fun getBuildTypes(@Param("project") project: String): TeamcityBuildTypes

    @RequestLine("POST $REST/buildTypes/{buildType}/features")
    @Headers("Content-Type: application/json")
    fun addBuildTypeFeature(@Param("buildType") buildType: String, feature: TeamcityLinkFeature)

    @RequestLine("GET $REST/buildTypes/{buildType}/features")
    @Headers("Accept: application/json")
    fun getBuildTypeFeatures(@Param("buildType") buildType: String): TeamcityFeatures

    @RequestLine("GET $REST/buildTypes/{buildType}/features/{feature}")
    @Headers("Accept: application/json")
    fun getBuildTypeFeature(@Param("buildType") buildType: String, @Param("feature") feature: String): TeamcityFeature

    @RequestLine("PUT $REST/buildTypes/{buildType}/features/{feature}/parameters/{parameter}")
    @Headers("Content-Type: text/plain")
    @Body("{newValue}")
    fun updateBuildTypeFeatureParameter(
        @Param("buildType") buildType: String,
        @Param("feature") feature: String,
        @Param("parameter") parameter: String,
        @Param("newValue") newValue: String
    )

    @RequestLine("GET $REST/buildTypes/{buildType}/features/{feature}/parameters/{parameter}")
    @Headers("Accept: text/plain")
    fun getBuildTypeFeatureParameter(
        @Param("buildType") buildType: String,
        @Param("feature") feature: String,
        @Param("parameter") parameter: String
    ): String

    @RequestLine("PUT $REST/buildTypes/{buildType}/settings/buildNumberCounter")
    @Headers("Content-Type: text/plain")
    @Body("{newValue}")
    fun setBuildCounter(@Param("buildType") buildType: String, @Param("newValue") newValue: String)

    @RequestLine("POST $REST/buildTypes/{buildType}/snapshot-dependencies")
    @Headers("Content-Type: application/json")
    fun createSnapshotDependency(@Param("buildType") buildType: String, dependency: TeamcitySnapshotDependency)

    @RequestLine("DELETE $REST/buildTypes/{buildType}/snapshot-dependencies/{dependency}")
    @Headers("Accept: application/json")
    fun deleteSnapshotDependency(
        @Param("buildType") buildType: String,
        @Param("dependency") dependency: String
    )

    @RequestLine("GET $REST/buildTypes/{buildType}/snapshot-dependencies")
    @Headers("Accept: application/json")
    fun getSnapshotDependencies(@Param("buildType") buildType: String): TeamcitySnapshotDependencies

    @RequestLine("PUT $REST/buildTypes/{buildType}/steps/{step}/disabled")
    @Headers("Content-Type: text/plain")
    @Body("{newValue}")
    fun disableBuildStep(
        @Param("buildType") buildType: String,
        @Param("step") step: String,
        @Param("newValue") newValue: Boolean
    )

    @RequestLine("POST $REST/buildTypes/{buildType}/steps")
    @Headers("Content-Type: application/json")
    fun createBuildStep(@Param("buildType") buildType: String, step: TeamcityStep)

    @RequestLine("GET $REST/buildTypes/{buildType}/steps")
    @Headers("Accept: application/json")
    fun getBuildSteps(@Param("buildType") buildType: String): TeamcitySteps

    @RequestLine("POST $REST/buildTypes/{buildType}/vcs-root-entries")
    @Headers("Content-Type: application/json")
    fun createBuildTypeVcsRootEntry(
        @Param("buildType") buildType: String,
        vcsRootEntry: TeamcityCreateVcsRootEntry
    )

    @RequestLine("DELETE $REST/buildTypes/{buildType}/vcs-root-entries/{vcsRootEntryId}")
    @Headers("Accept: application/json")
    fun deleteBuildTypeVcsRootEntry(
        @Param("buildType") buildType: String,
        @Param("vcsRootEntryId") vcsRootEntryId: String
    )

    @RequestLine("GET $REST/buildTypes/{buildType}/vcs-root-entries")
    @Headers("Accept: application/json")
    fun getBuildTypeVcsRootEntries(@Param("buildType") buildType: String): TeamcityVcsRootEntries

    @RequestLine("GET $REST/buildTypes/{buildType}/vcs-root-entries/{vcsRootEntryId}")
    @Headers("Accept: application/json")
    fun getBuildTypeVcsRootEntry(
        @Param("buildType") buildType: String,
        @Param("vcsRootEntryId") vcsRootEntryId: String
    ): TeamcityVcsRootEntry

    @RequestLine("PUT $REST/buildTypes/{buildType}/vcs-root-entries/{vcsRootEntryId}/checkout-rules")
    @Headers("Content-Type: text/plain")
    @Body("{checkoutRules}")
    fun updateBuildTypeVcsRootEntryCheckoutRules(
        @Param("buildType") buildType: String,
        @Param("vcsRootEntryId") vcsRootEntryId: String,
        @Param("checkoutRules") checkoutRules: String
    )

    @RequestLine("POST $REST/vcs-roots")
    @Headers("Accept: application/json", "Content-Type: application/json")
    fun createVcsRoot(vcsRoot: TeamcityCreateVcsRoot): TeamcityVcsRoot

    @RequestLine("GET $REST/vcs-roots/{vcsRootId}")
    @Headers("Accept: application/json")
    fun getVcsRoot(@Param("vcsRootId") vcsRootId: String): TeamcityVcsRoot

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

    @RequestLine("GET $REST/buildTypes/{buildType}/template")
    @Headers("Accept: text/plain")
    fun getBuildTypeTemplate(@Param("buildType") buildType: String): String

    @RequestLine("PUT $REST/buildTypes/{buildType}/template")
    @Headers("Content-Type: text/plain")
    @Body("{template}")
    fun attachTemplateToBuildType(@Param("buildType") buildType: String, @Param("template") template: String)

    @RequestLine("DELETE $REST/buildTypes/{buildType}/templates")
    fun detachTemplatesFromBuildType(@Param("buildType") buildType: String)

    @RequestLine("POST $REST/{type}/{id}/parameters")
    @Headers("Content-Type: application/json")
    fun createParameter(
        @Param("type") configurationType: ConfigurationType,
        @Param("id") id: String,
        parameter: TeamcityProperty
    )

    @RequestLine("POST $REST/{type}/{id}/parameters")
    @Headers("Content-Type: application/xml")
    @Body("<property name=\"{parameterName}\" value=\"{value}\"/>")
    fun createParameter(
        @Param("type") configurationType: ConfigurationType,
        @Param("id") id: String,
        @Param("parameterName") parameterName: String,
        @Param("value") value: String = ""
    )

    @RequestLine("PUT $REST/{type}/{id}/parameters/{parameterName}")
    @Headers("Content-Type: text/plain")
    @Body("{value}")
    fun setParameter(
        @Param("type") configurationType: ConfigurationType,
        @Param("id") id: String,
        @Param("parameterName") parameterName: String,
        @Param("value") value: String
    )

    @RequestLine("GET $REST/{type}/{id}/parameters/{parameterName}")
    @Headers("Accept: text/plain")
    fun getParameter(
        @Param("type") configurationType: ConfigurationType,
        @Param("id") id: String,
        @Param("parameterName") parameterName: String
    ): String

    @RequestLine("DELETE $REST/{type}/{id}/parameters/{parameterName}")
    fun deleteParameter(
        @Param("type") configurationType: ConfigurationType,
        @Param("id") id: String,
        @Param("parameterName") parameterName: String
    )

    @RequestLine("GET $REST/vcs-root-instances?locator={locator}")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getVcsRootInstancesByLocator(@Param("locator") locator: VcsRootInstanceLocator): TeamcityVcsRootInstances

    @RequestLine("GET $REST/vcs-roots?locator={locator}")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getVcsRootsByLocator(@Param("locator") locator: VcsRootLocator): TeamcityVcsRoots
}


enum class ConfigurationType(private val value: String) {
    PROJECT("projects"),
    BUILD_TYPE("buildTypes");

    override fun toString() = value
}
