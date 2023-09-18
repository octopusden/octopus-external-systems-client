package org.octopusden.octopus.infrastructure.gitea.client

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
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateOrganization
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreatePullRequest
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaCreateRepository
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaOrganization

class GiteaClassicClient(
    apiParametersProvider: ClientParametersProvider,
    mapper: ObjectMapper
) : GiteaClient {

    private val client: GiteaClient = createClient(
        apiParametersProvider.getApiUrl(),
        apiParametersProvider.getAuth().getInterceptor(),
        mapper
    )

    constructor(apiParametersProvider: ClientParametersProvider) : this(
        apiParametersProvider,
        getMapper()
    )

    override fun getOrganizations(requestParams: Map<String, Any>) =
        client.getOrganizations(requestParams)

    override fun getRepositories(requestParams: Map<String, Any>) =
        client.getRepositories(requestParams)

    override fun createOrganization(dto: GiteaCreateOrganization) = client.createOrganization(dto)

    override fun getOrganization(organization: String): GiteaOrganization = client.getOrganization(organization)

    override fun getRepositories(organization: String, requestParams: Map<String, Any>) =
        client.getRepositories(organization, requestParams)

    override fun getRepository(organization: String, repository: String) =
        client.getRepository(organization, repository)

    override fun createRepository(organization: String, dto: GiteaCreateRepository) =
        client.createRepository(organization, dto)

    override fun deleteRepository(organization: String, repository: String) =
        client.deleteRepository(organization, repository)

    override fun getCommits(
        organization: String,
        repository: String,
        requestParams: Map<String, Any>
    ) = client.getCommits(organization, repository, requestParams)

    override fun getCommit(organization: String, repository: String, sha: String) =
        client.getCommit(organization, repository, sha)

    override fun getTags(
        organization: String,
        repository: String,
        requestParams: Map<String, Any>
    ) = client.getTags(organization, repository, requestParams)

    override fun getBranches(
        organization: String,
        repository: String,
        requestParams: Map<String, Any>
    ) = client.getBranches(organization, repository, requestParams)

    override fun getDefaultReviewers(organization: String, repository: String) =
        client.getDefaultReviewers(organization, repository)

    override fun createPullRequest(
        organization: String,
        repository: String,
        dto: GiteaCreatePullRequest
    ) = client.createPullRequest(organization, repository, dto)

    companion object {
        private fun getMapper(): ObjectMapper {
            val objectMapper = jacksonObjectMapper()
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            return objectMapper
        }

        private fun createClient(
            apiUrl: String,
            interceptor: RequestInterceptor,
            objectMapper: ObjectMapper
        ): GiteaClient =
            Feign.builder()
                .client(ApacheHttpClient())
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .errorDecoder(GiteaClientErrorDecoder(objectMapper))
                .encoder(JacksonEncoder(objectMapper))
                .decoder(GiteaClientDecoder(objectMapper))
                .requestInterceptor(interceptor)
                .logger(Slf4jLogger(GiteaClient::class.java))
                .logLevel(Logger.Level.FULL)
                .target(GiteaClient::class.java, apiUrl)
    }
}
