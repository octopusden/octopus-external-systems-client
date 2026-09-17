package org.octopusden.octopus.infrastructure.artifactory.client

import feign.Headers
import feign.Param
import feign.RequestLine
import feign.Response
import org.octopusden.octopus.infrastructure.artifactory.client.dto.AqlSearchResponse
import org.octopusden.octopus.infrastructure.artifactory.client.dto.ArtifactoryResponse
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfo
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfoResponse
import org.octopusden.octopus.infrastructure.artifactory.client.dto.DeleteBuildRequest
import org.octopusden.octopus.infrastructure.artifactory.client.dto.PromoteBuildRequest
import org.octopusden.octopus.infrastructure.artifactory.client.dto.PromoteDockerImageRequest
import org.octopusden.octopus.infrastructure.artifactory.client.dto.SystemVersion
import org.octopusden.octopus.infrastructure.artifactory.client.dto.Tokens
import java.io.OutputStream

const val ARTIFACTORY = "artifactory"
const val ARTIFACTORY_PATH = "$ARTIFACTORY/api"
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
    fun getBuildInfo(
        @Param("buildName") buildName: String,
        @Param("buildNumber") buildNumber: String,
    ): BuildInfoResponse

    @RequestLine("PUT $BUILD_PATH")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun uploadBuildInfo(request: BuildInfo)

    @RequestLine("POST $BUILD_PATH/delete")
    @Headers("Content-Type: application/json")
    fun deleteBuild(request: DeleteBuildRequest)

    @RequestLine("POST $BUILD_PATH/promote/{buildName}/{buildNumber}")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun promoteBuild(
        @Param("buildName") buildName: String,
        @Param("buildNumber") buildNumber: String,
        request: PromoteBuildRequest,
    ): ArtifactoryResponse

    @RequestLine("POST $DOCKER_PATH/{repoKey}/v2/promote")
    @Headers("Content-Type: application/json")
    fun promoteDockerImage(
        @Param("repoKey") repoKey: String,
        request: PromoteDockerImageRequest,
    )

    @RequestLine("POST $ARTIFACTORY_PATH/search/aql")
    @Headers("Content-Type: text/plain", "Accept: application/json")
    fun searchByAQL(query: String): AqlSearchResponse

    /**
     * Downloads the artifact at the given path as a raw streaming response.
     *
     * The caller **must** close the returned [Response] (including on partial reads and exceptions),
     * otherwise HTTP connection pool resources will be leaked.
     * Prefer [downloadArtifactTo] for safe, automatic cleanup:
     * ```kotlin
     * client.downloadArtifact("my-repo/path/to/file.jar").use { response ->
     *     response.body().asInputStream().copyTo(destination)
     * }
     * ```
     */
    @RequestLine("GET $ARTIFACTORY/{artifactPath}")
    fun downloadArtifact(
        @Param("artifactPath") artifactPath: String,
    ): Response
}

fun ArtifactoryClient.downloadArtifactTo(
    artifactPath: String,
    destination: OutputStream,
) {
    downloadArtifact(artifactPath).use { response ->
        response.body().asInputStream().copyTo(destination)
    }
}
