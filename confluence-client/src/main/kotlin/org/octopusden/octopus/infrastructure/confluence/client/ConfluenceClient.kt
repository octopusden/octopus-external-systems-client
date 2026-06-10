package org.octopusden.octopus.infrastructure.confluence.client

import feign.Headers
import feign.Param
import feign.QueryMap
import feign.RequestLine
import org.octopusden.octopus.infrastructure.confluence.client.dto.ConfluencePage
import org.octopusden.octopus.infrastructure.confluence.client.dto.ConfluencePageUpdateRequest

const val REST_API_PATH = "rest/api"
const val CONTENT_PATH = "$REST_API_PATH/content"

const val DEFAULT_EXPAND = "body.storage,version,space,ancestors"

interface ConfluenceClient {

    @RequestLine("GET $CONTENT_PATH/{id}")
    fun getPageById(
        @Param("id") id: String,
        @QueryMap queryParams: Map<String, String>
    ): ConfluencePage

    @RequestLine("PUT $CONTENT_PATH/{id}")
    @Headers("Content-Type: application/json")
    fun updatePage(@Param("id") id: String, page: ConfluencePageUpdateRequest): ConfluencePage
}

fun ConfluenceClient.getPageById(
    id: String,
    expand: String = DEFAULT_EXPAND
): ConfluencePage = getPageById(id, mapOf("expand" to expand))
