package org.octopusden.octopus.infrastructure.teamcity.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildTypeLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.ProjectLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.PropertyLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootInstanceLocator

class LocatorTest {

    @Test
    fun testVcsRootInstancesLocator() {
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

    @Test
    fun testVcsRootsLocator() {
        val url = "https://github.com/github/training-kit.git"
        val expected =
            "count:99999,property:(ignoreCase:true,matchType:equals,name:url,value:$url),property:(ignoreCase:true,matchType:equals,name:branch,value:main)"
        val actual = VcsRootInstanceLocator(
            count = 99999,
            property = listOf(
                PropertyLocator(
                    name = "url",
                    value = url,
                    matchType = PropertyLocator.MatchType.EQUALS,
                    ignoreCase = true
                ),
                PropertyLocator(
                    name = "branch",
                    value = "main",
                    matchType = PropertyLocator.MatchType.EQUALS,
                    ignoreCase = true
                )
            )
        ).toString()
        assertEquals(expected, actual)
    }

    @Test
    fun testProjectLocator() {
        val expected = "count:2000,parameter:(name:ParameterName,value:ParameterValue)"
        val actual = ProjectLocator(
            count = 2000,
            parameters = listOf(
                PropertyLocator(
                    name = "ParameterName",
                    value = "ParameterValue"
                )
            )
        ).toString()
        assertEquals(expected, actual)
    }
}