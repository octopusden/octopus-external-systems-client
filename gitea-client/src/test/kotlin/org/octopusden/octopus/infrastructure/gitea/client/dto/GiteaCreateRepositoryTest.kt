package org.octopusden.octopus.infrastructure.gitea.client.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GiteaCreateRepositoryTest {
    private val mapper = jacksonObjectMapper()

    private fun json(dto: GiteaCreateRepository): JsonNode = mapper.readTree(mapper.writeValueAsString(dto))

    @Test
    fun testNameOnlyIsSerializedAsBefore() {
        assertEquals(mapper.readTree("""{"name":"repo"}"""), json(GiteaCreateRepository("repo")))
    }

    @Test
    fun testAllFieldsAreSerializedWithGiteaNames() {
        assertEquals(
            mapper.readTree(
                """{"name":"repo","description":"marker","private":true,"default_branch":"main"}""",
            ),
            json(GiteaCreateRepository("repo", "marker", true, "main")),
        )
    }

    @Test
    fun testPublicRepositoryIsSerialized() {
        val node = json(GiteaCreateRepository("repo", `private` = false))
        assertEquals(false, node["private"].asBoolean())
        assertEquals(false, node.has("default_branch"))
    }
}
