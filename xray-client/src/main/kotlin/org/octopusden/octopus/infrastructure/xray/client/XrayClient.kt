package org.octopusden.octopus.infrastructure.xray.client

import feign.Headers
import feign.RequestLine
import org.octopusden.octopus.infrastructure.xray.client.dto.ArtifactStatusRequest
import org.octopusden.octopus.infrastructure.xray.client.dto.ArtifactStatusResponse
import org.octopusden.octopus.infrastructure.xray.client.dto.ArtifactSummaryRequest
import org.octopusden.octopus.infrastructure.xray.client.dto.ComponentSummaryRequest
import org.octopusden.octopus.infrastructure.xray.client.dto.SummaryResponse

const val XRAY_PATH = "xray/api/v1"

/** JFrog Xray, at the JFrog platform URL; reads what Xray found in artifacts it indexed. */
interface XrayClient {
    /** The scan status of one artifact, overall and per scan type; license data comes from the SCA scan. */
    @RequestLine("POST $XRAY_PATH/artifact/status")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getArtifactStatus(request: ArtifactStatusRequest): ArtifactStatusResponse

    /**
     * The components and licenses Xray found in each artifact. A path Xray has not indexed comes
     * back in [SummaryResponse.errors], not as an HTTP error.
     */
    @RequestLine("POST $XRAY_PATH/summary/artifact")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getArtifactSummary(request: ArtifactSummaryRequest): SummaryResponse

    /**
     * The licenses of components by id (`gav://group:artifact:version`, `npm://name:version`).
     * Each answer's `component_id` is the id without its scheme.
     */
    @RequestLine("POST $XRAY_PATH/summary/component")
    @Headers("Content-Type: application/json", "Accept: application/json")
    fun getComponentSummary(request: ComponentSummaryRequest): SummaryResponse
}
