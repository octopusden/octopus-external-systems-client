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
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependencies
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependency
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityStep
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySteps
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootEntries
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityVcsRootEntry
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val _log: Logger = LoggerFactory.getLogger(TeamcityClient::class.java)
private const val API_VERSION: String = "2018.1"
private const val REST: String = "/app/rest/$API_VERSION"

interface TeamcityClient {

    @RequestLine("POST $REST/projects")
    @Headers("Content-Type: application/json")
    fun createProject(dto: TeamcityCreateProject)

    @RequestLine("DELETE $REST/projects/{project}")
    fun deleteProject(@Param("project") project: String)

    @RequestLine("GET $REST/projects/{project}")
    @Headers("Accept: application/json")
    fun getProject(@Param("project") project: String): TeamcityProject

    @RequestLine("POST $REST/buildTypes")
    @Headers("Content-Type: application/json")
    fun createBuildType(dto: TeamcityCreateBuildType)

    @RequestLine("DELETE $REST/buildTypes/{buildType}")
    fun deleteBuildType(@Param("buildType") buildType: String)

    @RequestLine("GET $REST/buildTypes/{buildType}")
    @Headers("Accept: application/json")
    fun getBuildType(@Param("buildType") buildType: String): TeamcityBuildType

    @RequestLine("GET $REST/buildTypes")
    @Headers("Accept: application/json")
    fun getBuildTypes(): TeamcityBuildTypes

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
//    @JsonRawValue
    fun getBuildTypeFeatureParameter(
        @Param("buildType") buildType: String,
        @Param("feature") feature: String,
        @Param("parameter") parameter: String
    ): String

    @RequestLine("PUT $REST/buildTypes/{buildType}/settings/buildNumberCounter")
    @Headers("Content-Type: text/plain")
    @Body("{newValue}")
    fun updateBuildCounter(@Param("buildType") buildType: String, @Param("newValue") newValue: String)

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

//        POST   "$baseUrl/httpAuth/app/rest/$apiVersion/buildTypes/id:${buildTypeId}       /vcs-root-entries"
//        DELETE "$baseUrl/httpAuth/app/rest/$apiVersion/buildTypes/id:${buildType.id}      /vcs-root-entries/id:${vcsRootEntry.id}"
//        DELETE "$baseUrl/httpAuth/app/rest/$apiVersion/buildTypes/id:$buildConfigurationId/vcs-root-entries/id:$vcsRootId"
//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/buildTypes/id:$buildConfigurationId/vcs-root-entries"
//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/buildTypes/id:$buildConfigurationId/vcs-root-entries/${vcsRootIds[0]}"
//        PUT    "$baseUrl/httpAuth/app/rest/$apiVersion/buildTypes/id:$buildConfigurationId/vcs-root-entries/$vcsRootEntryId/checkout-rules"
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
    @Headers("Accept: application/json","Content-Type: application/json")
    fun createVcsRoot(vcsRoot: TeamcityCreateVcsRoot): TeamcityVcsRoot

//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/vcs-roots/id:${it.id}"
//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/vcs-roots/id:${root.id}"
//        PUT    "$baseUrl/httpAuth/app/rest/$apiVersion/vcs-roots/id:${rootId}/properties/$propertyName"
    @RequestLine("GET $REST/vcs-roots/{vcsRootId}")
    @Headers("Accept: application/json")
    fun getVcsRoot(
        @Param("vcsRootId") vcsRootId: String
    ): TeamcityVcsRoot

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

// TODO:
//        DELETE "$baseUrl/httpAuth/app/rest/$apiVersion/$type/id:${id}/parameters/${parameterName}"
//        PUT    "$baseUrl/httpAuth/app/rest/$apiVersion/$type/id:${id}/parameters/${parameterName}"
//        ...
//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/buildTypes/id:$buildConfigurationId/template"
//        PUT    "$baseUrl/httpAuth/app/rest/$apiVersion/buildTypes/id:$configurationId/template"
//        DELETE "$baseUrl/httpAuth/app/rest/$apiVersion/buildTypes/id:$configurationId/templates"
//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/projects/${projectId}/parameters/${parameterName}/value"
//        PUT    "$baseUrl/httpAuth/app/rest/$apiVersion/projects/id:${projectId}/name"  body: "$name"
//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/projects/id:$projectId/buildTypes"
//        POST   "$baseUrl/httpAuth/app/rest/$apiVersion/projects/id:$projectId/buildTypes"
//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/projects?locator=parameter:(name:${pName},value:${pValue}),count:2000"
//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/vcs-root-instances?locator=property:(name:$fieldName,value:$it,matchType:equals,ignoreCase:true),count:99999,buildType:(id:$buildConfigurationId)"
//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/vcs-roots?locator=property:(name:url,value:$url,matchType:equals,ignoreCase:true),count:99999"
//        GET    "$baseUrl/httpAuth/app/rest/$apiVersion/vcs-roots?locator=property:(name:url,value:$url,matchType:equals,ignoreCase:true),property:(name:branch,value:$branch,matchType:equals,ignoreCase:true),count:99999"
}

open class Locator(private val locator: String) {
    constructor(locators: Map<String, String>) : this(
        locators.entries.joinToString(",", prefix = "?locator=", transform = { "${it.key}:${it.value}" })
    )

    override fun toString(): String {
        return locator
    }
}