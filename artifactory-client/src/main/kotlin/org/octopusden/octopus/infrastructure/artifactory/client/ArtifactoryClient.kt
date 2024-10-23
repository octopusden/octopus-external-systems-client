package org.octopusden.octopus.infrastructure.artifactory.client

import feign.Param
import feign.RequestLine
import org.octopusden.octopus.infrastructure.artifactory.client.dto.BuildInfoResponse
import org.octopusden.octopus.infrastructure.artifactory.client.dto.PromoteBuild
import org.octopusden.octopus.infrastructure.artifactory.client.dto.SystemVersion

const val ROOT_PATH = "api"
const val SYSTEM_PATH = "$ROOT_PATH/system"
const val BUILD_PATH = "${ROOT_PATH}/build"

interface ArtifactoryClient {
    @RequestLine("GET $SYSTEM_PATH/version")
    fun getVersion(): SystemVersion

    @RequestLine("GET $BUILD_PATH/{buildName}/{buildNumber}")
    fun getBuildInfo(@Param("buildName") buildName: String, @Param("buildNumber") buildNumber: String): BuildInfoResponse

    @RequestLine("POST $BUILD_PATH/promote/{buildName}/{buildNumber}")
    fun promoteBuild(@Param("buildName") buildName: String, @Param("buildNumber") buildNumber: String, promoteBuild: PromoteBuild)
}
