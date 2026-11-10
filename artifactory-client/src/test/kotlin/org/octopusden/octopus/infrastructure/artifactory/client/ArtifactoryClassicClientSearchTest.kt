package org.octopusden.octopus.infrastructure.artifactory.client

import feign.Client
import feign.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBearerTokenCredentialProvider

class ArtifactoryClassicClientSearchTest {
    private val requests = mutableListOf<String>()

    private fun client(body: String) =
        ArtifactoryClassicClient(
            object : ClientParametersProvider {
                override fun getApiUrl() = "http://artifactory.example.com"

                override fun getAuth() = StandardBearerTokenCredentialProvider("token")
            },
            feignClient =
                Client { request, _ ->
                    requests += "${request.httpMethod()} ${request.url()}"
                    Response
                        .builder()
                        .status(200)
                        .request(request)
                        .headers(mapOf("Content-Type" to listOf("application/json")))
                        .body(body.toByteArray())
                        .build()
                },
        )

    @Test
    @DisplayName("An AQL item carries the checksums an items.find query includes")
    fun aqlItemChecksums() {
        val item =
            client(
                """{"results":[{"repo":"libs-release-local","path":"org/example/lib/1.0","name":"lib-1.0.jar",
                   "actual_sha1":"0a1b","sha256":"9f8e","properties":[{"key":"build.name","value":"lib_ee"}]}]}""",
            ).searchByAQL("""items.find({"name":"lib-1.0.jar"})""").results.single()

        assertEquals("0a1b", item.actualSha1)
        assertEquals("9f8e", item.sha256)
        assertEquals("lib_ee", item.properties!!.single().value)
    }

    @Test
    @DisplayName("A builds.find query answers build names and numbers")
    fun buildSearch() {
        val builds =
            client("""{"results":[{"build.name":"lib_ee","build.number":"1.0-12","build.repo":"artifactory-build-info"}]}""")
                .searchBuildsByAQL("""builds.find({"module.artifact.sha1":"0a1b"})""")
                .results

        assertEquals("lib_ee", builds.single().name)
        assertEquals("1.0-12", builds.single().number)
        assertEquals(listOf("POST http://artifactory.example.com/artifactory/api/search/aql"), requests)
    }

    @Test
    @DisplayName("Build-info can be read from a named build-info repository, with the path of each artifact")
    fun buildInfoFromRepository() {
        val buildInfo =
            client(
                """{"buildInfo":{"name":"app_ee_docker","number":"2.4.0","modules":[{"id":"sample/app:2.4.0","type":"docker",
                   "artifacts":[{"name":"manifest.json","sha1":"aa","path":"sample/app/2.4.0/manifest.json"}]}]}}""",
            ).getBuildInfo("app_ee_docker", "2.4.0", "artifactory-build-info").buildInfo

        assertEquals(
            "sample/app/2.4.0/manifest.json",
            buildInfo.modules!!
                .single()
                .artifacts!!
                .single()
                .path,
        )
        assertEquals(
            listOf("GET http://artifactory.example.com/artifactory/api/build/app_ee_docker/2.4.0?buildRepo=artifactory-build-info"),
            requests,
        )
    }
}
