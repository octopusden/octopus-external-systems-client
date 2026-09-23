package org.octopusden.octopus.infrastructure.artifactory.client

import feign.Client
import feign.Response
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.octopusden.octopus.infrastructure.artifactory.client.exception.InternalServerError
import org.octopusden.octopus.infrastructure.artifactory.client.exception.NotFoundException
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream

class ArtifactoryClassicClientTest {
    private fun stubClient(
        status: Int,
        body: ByteArray? = "content".toByteArray(),
        onRequest: (String) -> Unit = {},
    ): Client =
        Client { request, _ ->
            onRequest(request.url())
            val builder =
                Response
                    .builder()
                    .status(status)
                    .request(request)
                    .headers(emptyMap())
            if (body != null) builder.body(body)
            builder.build()
        }

    private fun makeClient(
        status: Int,
        body: ByteArray? = "content".toByteArray(),
        onRequest: (String) -> Unit = {},
    ): ArtifactoryClassicClient =
        ArtifactoryClassicClient(
            object : ClientParametersProvider {
                override fun getApiUrl() = "http://artifactory.example.com"

                override fun getAuth() = StandardBasicCredCredentialProvider("user", "pass")
            },
            feignClient = stubClient(status, body, onRequest),
        )

    @Test
    fun `downloadArtifact builds correct URL`() {
        var capturedUrl: String? = null
        makeClient(200, onRequest = { capturedUrl = it }).downloadArtifact("my-repo/path/file.jar").close()
        assertEquals("http://artifactory.example.com/artifactory/my-repo/path/file.jar", capturedUrl)
    }

    @Test
    fun `downloadArtifact 200 returns body without consuming it`() {
        val content = "artifact-bytes".toByteArray()
        makeClient(200, content).downloadArtifact("repo/file.jar").use { response ->
            assertEquals(200, response.status())
            assertArrayEquals(content, response.body().asInputStream().readBytes())
        }
    }

    @Test
    fun `downloadArtifact 404 throws NotFoundException`() {
        assertThrows<NotFoundException> {
            makeClient(404).downloadArtifact("repo/missing.jar")
        }
    }

    @Test
    fun `downloadArtifact 500 throws InternalServerError`() {
        assertThrows<InternalServerError> {
            makeClient(500).downloadArtifact("repo/file.jar")
        }
    }

    @Test
    fun `downloadArtifact null body on error does not throw NullPointerException`() {
        assertThrows<NotFoundException> {
            makeClient(404, body = null).downloadArtifact("repo/missing.jar")
        }
    }

    @Test
    fun `downloadArtifactTo copies body to destination`() {
        val content = "artifact-content".toByteArray()
        val destination = ByteArrayOutputStream()
        makeClient(200, content).downloadArtifactTo("repo/file.jar", destination)
        assertArrayEquals(content, destination.toByteArray())
    }

    @Test
    fun `downloadArtifactTo propagates destination exception`() {
        val failingDestination =
            object : OutputStream() {
                override fun write(b: Int) = throw IOException("disk full")
            }
        assertThrows<IOException> {
            makeClient(200, "data".toByteArray()).downloadArtifactTo("repo/file.jar", failingDestination)
        }
    }
}
