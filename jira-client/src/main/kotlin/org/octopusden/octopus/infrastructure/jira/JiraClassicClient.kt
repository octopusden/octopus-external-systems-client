package org.octopusden.octopus.infrastructure.jira

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.Logger
import feign.RequestInterceptor
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.jira.dto.*

class JiraClassicClient(
    apiParametersProvider: ClientParametersProvider,
    mapper: ObjectMapper
) : JiraClient {

    private val client: JiraClient = createClient(
        apiParametersProvider.getApiUrl(),
        apiParametersProvider.getAuth().getInterceptor(),
        mapper
    )

    constructor(apiParametersProvider: ClientParametersProvider) : this(
        apiParametersProvider,
        getMapper()
    )

    override fun createIssue(issue: Issue<CreateIssueFields>) = client.createIssue(issue)

    override fun updateIssue(issueKey: String, issue: Issue<UpdateIssueFields>) = client.updateIssue(issueKey, issue)

    override fun getAssignable(issueKey: String, username: String?) = client.getAssignable(issueKey, username)

    override fun getActiveSprint(boardId: Long): ActiveSprintResponse = client.getActiveSprint(boardId)

    override fun moveIssuesToSprint(sprintId: Long, moveIssuesToSprintRequest: MoveIssuesToSprintRequest): Unit =
        client.moveIssuesToSprint(sprintId, moveIssuesToSprintRequest)


    override fun addRemoteLink(issueKey: String, remoteLinkRequest: RemoteLinkRequest): RemoteLinkResponse =
        client.addRemoteLink(issueKey, remoteLinkRequest)

    override fun searchIssueWithJql(searchIssueRequest: SearchIssueRequest): SearchIssueResponse =
        client.searchIssueWithJql(searchIssueRequest)

    companion object {
        private fun getMapper(): ObjectMapper {
            val objectMapper = jacksonObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            return objectMapper
        }

        private fun createClient(
            apiUrl: String, interceptor: RequestInterceptor, objectMapper: ObjectMapper
        ) = Feign.builder()
            .client(ApacheHttpClient())
            .encoder(JacksonEncoder(objectMapper))
            .decoder(JacksonDecoder(objectMapper))
//            .errorDecoder(JiraClientErrorDecoder(objectMapper))
            .encoder(JacksonEncoder(objectMapper))
            .decoder(JacksonDecoder(objectMapper))
            .requestInterceptor(interceptor)
            .logger(Slf4jLogger(JiraClient::class.java))
            .logLevel(Logger.Level.FULL)
            .target(JiraClient::class.java, apiUrl)
    }
}
