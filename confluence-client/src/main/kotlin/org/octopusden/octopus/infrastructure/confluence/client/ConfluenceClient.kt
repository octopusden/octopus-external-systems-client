package org.octopusden.octopus.infrastructure.confluence.client

import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import org.octopusden.octopus.infrastructure.confluence.client.dto.ConfluencePage
import org.octopusden.octopus.infrastructure.confluence.client.dto.ConfluencePageCreateRequest
import org.octopusden.octopus.infrastructure.confluence.client.dto.ConfluencePageUpdateRequest
import org.octopusden.octopus.infrastructure.confluence.client.dto.ConfluenceSearchResponse

const val REST_API_PATH = "rest/api"
const val CONTENT_PATH = "$REST_API_PATH/content"

interface ConfluenceClient {
    @RequestLine("GET $CONTENT_PATH/{id}")
    fun getPageById(
        @Param("id") id: String,
        @QueryMap queryParams: Map<String, String>,
    ): ConfluencePage

    @RequestLine("PUT $CONTENT_PATH/{id}")
    @Headers("Content-Type: application/json")
    fun updatePage(
        @Param("id") id: String,
        page: ConfluencePageUpdateRequest,
    ): ConfluencePage

    @RequestLine("GET $CONTENT_PATH")
    fun searchPages(
        @QueryMap queryParams: Map<String, String>,
    ): ConfluenceSearchResponse

    @RequestLine("POST $CONTENT_PATH")
    @Headers("Content-Type: application/json")
    fun createPage(request: ConfluencePageCreateRequest): ConfluencePage
}
