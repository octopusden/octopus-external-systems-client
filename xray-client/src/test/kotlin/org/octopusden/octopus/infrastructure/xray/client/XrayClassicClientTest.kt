package org.octopusden.octopus.infrastructure.xray.client

import feign.Client
import feign.Response
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBearerTokenCredentialProvider
import org.octopusden.octopus.infrastructure.xray.client.dto.ArtifactStatusRequest
import org.octopusden.octopus.infrastructure.xray.client.dto.ArtifactSummaryRequest
import org.octopusden.octopus.infrastructure.xray.client.dto.ComponentDetail
import org.octopusden.octopus.infrastructure.xray.client.dto.ComponentSummaryRequest
import org.octopusden.octopus.infrastructure.xray.client.dto.ScanState
import org.octopusden.octopus.infrastructure.xray.client.exception.XrayClientException
import org.octopusden.octopus.infrastructure.xray.client.exception.XrayNotFoundException

/** The recorded answers under `resources/xray` come from Xray 3.150, names replaced; `status-pending.json` is written by hand. */
class XrayClassicClientTest {
    private val requests = mutableListOf<Pair<String, String>>()

    private fun client(
        fixture: String? = null,
        status: Int = 200,
        body: String = fixture?.let { javaClass.getResource("/xray/$it")!!.readText() }.orEmpty(),
    ) = XrayClassicClient(
        object : ClientParametersProvider {
            override fun getApiUrl() = "https://jfrog.example.com"

            override fun getAuth() = StandardBearerTokenCredentialProvider("token")
        },
        feignClient =
            Client { request, _ ->
                requests += "${request.httpMethod()} ${request.url()}" to String(request.body() ?: ByteArray(0))
                Response
                    .builder()
                    .status(status)
                    .request(request)
                    .headers(mapOf("Content-Type" to listOf("application/json")))
                    .body(body.toByteArray())
                    .build()
            },
    )

    @Test
    @DisplayName("Artifact status reads the overall and the SCA scan status")
    fun statusDone() {
        val status =
            client("status-done.json").getArtifactStatus(
                ArtifactStatusRequest("libs-release-local", "org/example/app/sample-app/2.4.0/sample-app-2.4.0.zip"),
            )

        assertEquals(ScanState.DONE, status.overall!!.status)
        assertEquals(ScanState.DONE, status.details!!.sca!!.status)
        assertEquals("POST https://jfrog.example.com/xray/api/v1/artifact/status", requests.single().first)
        assertEquals(
            """{"repo":"libs-release-local","path":"org/example/app/sample-app/2.4.0/sample-app-2.4.0.zip"}""",
            requests.single().second,
        )
    }

    @Test
    @DisplayName("A file Xray cannot scan is NOT_SUPPORTED")
    fun statusNotSupported() {
        val status = client("status-not-supported.json").getArtifactStatus(ArtifactStatusRequest("r", "a.pom"))

        assertEquals(ScanState.NOT_SUPPORTED, status.details!!.sca!!.status)
    }

    @Test
    @DisplayName("A scan in progress reads as PENDING overall and SCANNING for SCA")
    fun statusPending() {
        val status = client("status-pending.json").getArtifactStatus(ArtifactStatusRequest("r", "a.zip"))

        assertEquals(ScanState.PENDING, status.overall!!.status)
        assertEquals(ScanState.SCANNING, status.details!!.sca!!.status)
    }

    @Test
    @DisplayName("A status this client does not know reads as UNKNOWN rather than failing")
    fun statusUnknown() {
        val status =
            client(body = """{"overall":{"status":"RE_SCANNING"},"details":{"sca":{"status":"RE_SCANNING"}}}""")
                .getArtifactStatus(ArtifactStatusRequest("r", "a.zip"))

        assertEquals(ScanState.UNKNOWN, status.details!!.sca!!.status)
    }

    @Test
    @DisplayName("A missing artifact's status is an XrayClientException")
    fun statusMissing() {
        val error =
            assertThrows<XrayClientException> {
                client(status = 400, body = """{"error":"Resource does not exist in Artifactory"}""")
                    .getArtifactStatus(ArtifactStatusRequest("r", "missing.zip"))
            }

        assertEquals(400, error.status)
    }

    @Test
    @DisplayName("Artifact summary lists each license with the components found under it")
    fun artifactSummary() {
        val summary =
            client("artifact-summary.json").getArtifactSummary(
                ArtifactSummaryRequest(listOf("default/libs-release-local/org/example/app/sample-app/2.4.0/sample-app-2.4.0.zip")),
            )

        val licenses = summary.artifacts.single().licenses
        assertEquals(listOf("GPL-2.0+CE", "Unknown"), licenses.map { it.name })
        assertEquals(
            listOf("gav://org.glassfish.ha:ha-api:3.1.13", "gav://jakarta.ws.rs:jakarta.ws.rs-api:3.0.0"),
            licenses.first().components,
        )
        assertEquals(emptyList<Any>(), summary.errors)
        assertEquals("POST https://jfrog.example.com/xray/api/v1/summary/artifact", requests.single().first)
    }

    @Test
    @DisplayName("An artifact Xray has not indexed comes back as an error entry, not an HTTP error")
    fun artifactSummaryNotIndexed() {
        val summary = client("artifact-summary-not-indexed.json").getArtifactSummary(ArtifactSummaryRequest(listOf("default/r/a.pom")))

        assertEquals(emptyList<Any>(), summary.artifacts)
        assertEquals("Artifact doesn't exist or not indexed/cached in Xray", summary.errors.single().error)
    }

    @Test
    @DisplayName("Component summary is asked by scheme-prefixed ids and answers ids without the scheme")
    fun componentSummary() {
        val summary =
            client("component-summary.json").getComponentSummary(
                ComponentSummaryRequest(
                    listOf(ComponentDetail("gav://com.google.guava:guava:32.1.2-jre"), ComponentDetail("gav://no.such:thing:1")),
                ),
            )

        assertEquals(
            listOf("no.such:thing:1" to listOf("Unknown"), "com.google.guava:guava:32.1.2-jre" to listOf("Apache-2.0")),
            summary.artifacts.map { it.general!!.componentId to it.licenses.map { license -> license.name } },
        )
        assertEquals(
            """{"component_details":[""" +
                """{"component_id":"gav://com.google.guava:guava:32.1.2-jre"},{"component_id":"gav://no.such:thing:1"}]}""",
            requests.single().second,
        )
    }

    @Test
    @DisplayName("404 is XrayNotFoundException, other errors XrayClientException with their status")
    fun errors() {
        assertThrows<XrayNotFoundException> { client(status = 404, body = "").getComponentSummary(ComponentSummaryRequest(emptyList())) }
        assertEquals(
            503,
            assertThrows<XrayClientException> {
                client(status = 503, body = "<html>maintenance</html>").getArtifactSummary(ArtifactSummaryRequest(emptyList()))
            }.status,
        )
    }
}
