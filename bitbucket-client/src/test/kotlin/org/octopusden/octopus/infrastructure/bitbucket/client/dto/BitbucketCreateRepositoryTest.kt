package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BitbucketCreateRepositoryTest {
    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    private fun json(dto: BitbucketCreateRepository): JsonNode = mapper.readTree(mapper.writeValueAsString(dto))

    @Test
    fun testDefaultsAreSerializedAsBefore() {
        assertEquals(
            mapper.readTree("""{"name":"repo","forkable":false,"scmId":"git","defaultBranch":"master"}"""),
            json(BitbucketCreateRepository("repo")),
        )
    }

    @Test
    fun testDescriptionAndPublicAreSerialized() {
        val node = json(BitbucketCreateRepository("repo", description = "marker", public = true))
        assertEquals("marker", node["description"].asText())
        assertEquals(true, node["public"].asBoolean())
        assertEquals("repo", node["name"].asText())
    }

    @Test
    fun testPrivateRepositoryIsSerialized() {
        val node = json(BitbucketCreateRepository("repo", description = null, public = false))
        assertEquals(false, node["public"].asBoolean())
        assertEquals(false, node.has("description"))
    }

    @Test
    fun testRepositoryResponseExposesDescriptionAndPublic() {
        val repository = mapper.readValue<BitbucketRepository>(
            """
            {"id":1,"name":"repo","slug":"repo","description":"marker","public":false,"state":"AVAILABLE",
             "links":{"clone":[],"self":[]},"project":{"key":"P","id":1,"name":"P"}}
            """.trimIndent(),
        )
        assertEquals("marker", repository.description)
        assertEquals(false, repository.public)
    }
}
