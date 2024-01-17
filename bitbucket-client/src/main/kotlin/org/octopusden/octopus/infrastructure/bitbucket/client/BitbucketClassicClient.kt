package org.octopusden.octopus.infrastructure.bitbucket.client

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
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketAuthor
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketBranch
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCommit
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateProject
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreatePullRequest
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateRepository
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketEntityList
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketJiraCommit
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketProject
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketPullRequest
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketRepository
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketTag
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketUpdateRepository
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.DefaultReviewersQuery

class BitbucketClassicClient(
    apiParametersProvider: BitbucketClientParametersProvider,
    mapper: ObjectMapper
) : BitbucketClient {

    private val client: BitbucketClient = createClient(
        apiParametersProvider.getApiUrl(),
        apiParametersProvider.getAuth().getInterceptor(),
        mapper
    )

    constructor(apiParametersProvider: BitbucketClientParametersProvider) : this(
        apiParametersProvider,
        getMapper()
    )

    override fun getProjects(requestParams: Map<String, Any>): BitbucketEntityList<BitbucketProject> {
        return client.getProjects(requestParams)
    }

    override fun getRepositories(requestParams: Map<String, Any>): BitbucketEntityList<BitbucketRepository> {
        return client.getRepositories(requestParams)
    }

    override fun createProject(dto: BitbucketCreateProject) {
        client.createProject(dto)
    }

    override fun getProject(projectKey: String): BitbucketProject {
        return client.getProject(projectKey)
    }

    override fun getRepositories(
        projectKey: String,
        requestParams: Map<String, Any>
    ): BitbucketEntityList<BitbucketRepository> {
        return client.getRepositories(projectKey, requestParams)
    }

    override fun getRepository(projectKey: String, repository: String): BitbucketRepository {
        return client.getRepository(projectKey, repository)
    }

    override fun createRepository(projectKey: String, dto: BitbucketCreateRepository) {
        client.createRepository(projectKey, dto)
    }

    override fun updateRepository(projectKey: String, repository: String, dto: BitbucketUpdateRepository) {
        return client.updateRepository(projectKey, repository, dto)
    }

    override fun deleteRepository(projectKey: String, repository: String) {
        return client.deleteRepository(projectKey, repository)
    }

    override fun _getCommits(
        projectKey: String,
        repository: String,
        requestParams: Map<String, Any>
    ): BitbucketEntityList<BitbucketCommit> {
        return client._getCommits(projectKey, repository, requestParams)
    }

    override fun _getCommit(projectKey: String, repository: String, id: String): BitbucketCommit {
        return client._getCommit(projectKey, repository, id)
    }

    override fun _getCommits(
        issueKey: String,
        requestParams: Map<String, Any>
    ): BitbucketEntityList<BitbucketJiraCommit> {
        return client._getCommits(issueKey, requestParams)
    }

    override fun getTags(
        projectKey: String,
        repository: String,
        requestParams: Map<String, Any>
    ): BitbucketEntityList<BitbucketTag> {
        return client.getTags(projectKey, repository, requestParams)
    }

    override fun getBranches(
        projectKey: String,
        repository: String,
        requestParams: Map<String, Any>
    ): BitbucketEntityList<BitbucketBranch> {
        return client.getBranches(projectKey, repository, requestParams)
    }

    override fun getDefaultReviewers(
        projectKey: String,
        repository: String,
        query: DefaultReviewersQuery
    ): Set<BitbucketAuthor> = client.getDefaultReviewers(projectKey, repository, query)

    override fun createPullRequest(
        projectKey: String,
        repository: String,
        dto: BitbucketCreatePullRequest
    ): BitbucketPullRequest {
        return client.createPullRequest(projectKey, repository, dto)
    }

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
        ): BitbucketClient {
            return Feign.builder()
                .client(ApacheHttpClient())
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .errorDecoder(BitbucketClientErrorDecoder(objectMapper))
                .encoder(JacksonEncoder(objectMapper))
                .decoder(JacksonDecoder(objectMapper))
                .requestInterceptor(interceptor)
                .logger(Slf4jLogger(BitbucketClient::class.java))
                .logLevel(Logger.Level.FULL)
                .target(BitbucketClient::class.java, apiUrl)
        }
    }
}
