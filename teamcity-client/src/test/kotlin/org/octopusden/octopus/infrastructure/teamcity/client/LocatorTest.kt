package org.octopusden.octopus.infrastructure.teamcity.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LocatorTest{
    @Test
    fun testId(){
        assertEquals(Locator("testId").toString(), "testId")
    }

    @Test
    fun testLocator(){
        val locator = Locator(mapOf(
            "name" to "BuildName",
            "type" to "regular"
        ))
        assertEquals( "$locator", "?locator=name:BuildName,type:regular")
    }
}