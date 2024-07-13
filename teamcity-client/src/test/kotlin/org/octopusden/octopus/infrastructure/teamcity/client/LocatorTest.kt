package org.octopusden.octopus.infrastructure.teamcity.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildTypeLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.PropertyLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootInstanceLocator

class LocatorTest {

    @Test
    fun testVcsRootInstanceLocator() {
        val expected =
            "buildType:(id:buildConfigurationId),count:99999,property:(ignoreCase:true,matchType:equals,name:fieldName,value:fieldValue)"
        val actual = VcsRootInstanceLocator(
            count = 99999,
            buildType = BuildTypeLocator(id = "buildConfigurationId"),
            property = listOf(
                PropertyLocator(
                    name = "fieldName",
                    value = "fieldValue",
                    matchType = PropertyLocator.MatchType.EQUALS,
                    ignoreCase = true
                )
            )
        ).toString()
        assertEquals(expected, actual)
    }
}