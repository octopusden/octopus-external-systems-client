package org.octopusden.octopus.infrastructure.artifactory.client

import feign.Headers
import feign.Param
import feign.RequestLine
import org.octopusden.octopus.infrastructure.artifactory.client.dto.ArtifactoryResponse
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfoResponse
import org.octopusden.octopus.infrastructure.artifactory.client.dto.PromoteBuildRequest
import org.octopusden.octopus.infrastructure.artifactory.client.dto.PromoteDockerImageRequest
import org.octopusden.octopus.infrastructure.artifactory.client.dto.SystemVersion
import org.octopusden.octopus.infrastructure.artifactory.client.dto.Tokens

const val ARTIFACTORY_PATH = "artifactory/api"
const val ACCESS_PATH = "access/api/v1"
const val TOKENS_PATH = "$ACCESS_PATH/tokens"
const val SYSTEM_PATH = "$ARTIFACTORY_PATH/system"
const val BUILD_PATH = "$ARTIFACTORY_PATH/build"
const val DOCKER_PATH = "$ARTIFACTORY_PATH/docker"

interface ArtifactoryClient {
    @RequestLine("GET $TOKENS_PATH")
    @Headers("Accept: application/json")
    fun getTokens(): Tokens

    @RequestLine("GET $SYSTEM_PATH/version")
    @Headers("Accept: application/json")
    fun getVersion(): SystemVersion

    @RequestLine("GET $BUILD_PATH/{buildName}/{buildNumber}")
    @Headers("Accept: application/json")
    fun getBuildInfo(@Param("buildName") buildName: String, @Param("buildNumber") buildNumber: String): BuildInfoResponse

    @RequestLine("POST $BUILD_PATH/promote/{buildName}/{buildNumber}")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun promoteBuild(@Param("buildName") buildName: String, @Param("buildNumber") buildNumber: String, request: PromoteBuildRequest): ArtifactoryResponse

    @RequestLine("POST $DOCKER_PATH/{repoKey}/v2/promote")
    @Headers("Content-Type: application/json")
    fun promoteDockerImage(@Param("repoKey") repoKey: String, request: PromoteDockerImageRequest)
}
