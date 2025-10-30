package org.octopusden.octopus.infrastructure.bitbucket.client

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import feign.Feign
import feign.Logger
import feign.RequestInterceptor
import feign.httpclient.ApacheHttpClient
import feign.jackson.JacksonEncoder
import feign.slf4j.Slf4jLogger
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateProject
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreatePullRequest
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateRepository
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketCreateTag
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketDeleteBranch
import org.octopusden.octopus.infrastructure.bitbucket.client.dto.BitbucketDeletePullRequest
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

    override fun getProjects(requestParams: Map<String, Any>) = client.getProjects(requestParams)

    override fun getRepositories(requestParams: Map<String, Any>) = client.getRepositories(requestParams)

    override fun createProject(dto: BitbucketCreateProject) = client.createProject(dto)

    override fun getProject(projectKey: String) = client.getProject(projectKey)

    override fun getRepositories(projectKey: String, requestParams: Map<String, Any>) =
        client.getRepositories(projectKey, requestParams)

    override fun getRepository(projectKey: String, repository: String) = client.getRepository(projectKey, repository)

    override fun createRepository(projectKey: String, dto: BitbucketCreateRepository) =
        client.createRepository(projectKey, dto)

    override fun updateRepository(projectKey: String, repository: String, dto: BitbucketUpdateRepository) =
        client.updateRepository(projectKey, repository, dto)

    override fun deleteRepository(projectKey: String, repository: String) =
        client.deleteRepository(projectKey, repository)

    override fun getCommits(projectKey: String, repository: String, requestParams: Map<String, Any>) =
        client.getCommits(projectKey, repository, requestParams)

    override fun _getCommit(projectKey: String, repository: String, id: String) =
        client._getCommit(projectKey, repository, id)

    override fun _getCommitChanges(projectKey: String, repository: String, id: String) =
        client._getCommitChanges(projectKey, repository, id)

    override fun getCommits(issueKey: String, requestParams: Map<String, Any>) =
        client.getCommits(issueKey, requestParams)

    override fun getTags(projectKey: String, repository: String, requestParams: Map<String, Any>) =
        client.getTags(projectKey, repository, requestParams)

    override fun createTag(projectKey: String, repository: String, dto: BitbucketCreateTag) =
        client.createTag(projectKey, repository, dto)

    override fun deleteTag(projectKey: String, repository: String, tag: String) =
        client.deleteTag(projectKey, repository, tag)

    override fun getBranches(projectKey: String, repository: String, requestParams: Map<String, Any>) =
        client.getBranches(projectKey, repository, requestParams)

    override fun deleteBranch(projectKey: String, repository: String, dto: BitbucketDeleteBranch) =
        client.deleteBranch(projectKey, repository, dto)

    override fun getDefaultReviewers(projectKey: String, repository: String, query: DefaultReviewersQuery) =
        client.getDefaultReviewers(projectKey, repository, query)

    override fun createPullRequest(projectKey: String, repository: String, dto: BitbucketCreatePullRequest) =
        client.createPullRequest(projectKey, repository, dto)

    override fun getPullRequest(projectKey: String, repository: String, id: Long) =
        client.getPullRequest(projectKey, repository, id)

    override fun getPullRequests( projectKey: String, repository: String, requestParams: Map<String, Any> ) =
        client.getPullRequests(projectKey, repository, requestParams)

    override fun getPullRequests(requestParams: Map<String, Any>) =
        client.getPullRequests(requestParams)

    override fun deletePullRequest(projectKey: String, repository: String, pullRequestId: String, dto: BitbucketDeletePullRequest) =
        client.deletePullRequest(projectKey, repository, pullRequestId, dto)

    override fun getRepositoryFiles(projectKey: String, repository: String, requestParams: Map<String, Any>) =
        client.getRepositoryFiles(projectKey, repository, requestParams)

    override fun getRepositoryRawFileContent(projectKey: String, repository: String, filePath: String) =
        client.getRepositoryRawFileContent(projectKey, repository, filePath)

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
                .decoder(BitbucketClientResponseDecoder(objectMapper))
                .errorDecoder(BitbucketClientErrorDecoder(objectMapper))
                .requestInterceptor(interceptor)
                .logger(Slf4jLogger(BitbucketClient::class.java))
                .logLevel(Logger.Level.FULL)
                .target(BitbucketClient::class.java, apiUrl)
        }
    }
}
