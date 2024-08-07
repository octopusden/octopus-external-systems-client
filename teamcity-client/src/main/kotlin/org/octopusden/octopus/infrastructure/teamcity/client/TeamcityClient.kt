package org.octopusden.octopus.infrastructure.teamcity.client

import com.fasterxml.jackson.annotation.JsonValue
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
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildTypeLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.ProjectLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootInstanceLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootLocator
import org.octopusden.octopus.infrastructure.teamcity.client.TeamcityLocatorExpander as Locator

private const val API_VERSION: String = "2018.1"
private const val REST: String = "/app/rest/$API_VERSION"

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
        @Param("locator", expander = Locator::class) project: ProjectLocator,
        @Param("name") name: String
    ): TeamcityBuildType

    @RequestLine("DELETE $REST/buildTypes/{locator}")
    fun deleteBuildType(@Param("locator", expander = Locator::class) buildType: BuildTypeLocator)

    @RequestLine("GET $REST/buildTypes/{locator}")
    @Headers("Accept: application/json")
    fun getBuildType(@Param("locator", expander = Locator::class) buildType: BuildTypeLocator): TeamcityBuildType

    @RequestLine("GET $REST/buildTypes")
    @Headers("Accept: application/json")
    fun getBuildTypes(): TeamcityBuildTypes

    @RequestLine("GET $REST/projects/{locator}/buildTypes")
    @Headers("Accept: application/json")
    fun getBuildTypes(@Param("locator", expander = Locator::class) project: ProjectLocator): TeamcityBuildTypes

    @RequestLine("POST $REST/buildTypes/{locator}/features")
    @Headers("Content-Type: application/json")
    fun addBuildTypeFeature(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        feature: TeamcityLinkFeature
    )

    @RequestLine("GET $REST/buildTypes/{locator}/features")
    @Headers("Accept: application/json")
    fun getBuildTypeFeatures(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator
    ): TeamcityFeatures

    @RequestLine("GET $REST/buildTypes/{locator}/features/{feature}")
    @Headers("Accept: application/json")
    fun getBuildTypeFeature(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("feature") feature: String
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
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("newValue") newValue: String
    )

    @RequestLine("POST $REST/buildTypes/{locator}/snapshot-dependencies")
    @Headers("Content-Type: application/json")
    fun createSnapshotDependency(
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        dependency: TeamcitySnapshotDependency
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
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        step: TeamcityStep
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
        @Param("locator", expander = Locator::class) buildType: BuildTypeLocator,
        @Param("template") template: String
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
}


enum class ConfigurationType(
    @get:JsonValue
    val type: String
) {
    PROJECT("projects"),
    BUILD_TYPE("buildTypes");

    class ConfigurationTypeExpander : Param.Expander {
        override fun expand(value: Any?) = when (value) {
            is ConfigurationType -> value.type
            else -> throw Exception("Unknown class ${value}")
        }
    }
}
