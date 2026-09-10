package org.octopusden.octopus.infrastructure.teamcity.client

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildTypeLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.InvestigationLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.ProjectLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.PropertyLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.TemplateLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootInstanceLocator

class LocatorTest {
    private val locatorExpander: TeamcityLocatorExpander = TeamcityLocatorExpander()

    @Test
    fun testVcsRootInstancesLocator() {
        val expected =
            "buildType:(id:buildConfigurationId),count:99999,property:(ignoreCase:true,matchType:equals,name:fieldName,value:fieldValue)"
        val actual = locatorExpander.expand(
            VcsRootInstanceLocator(
                count = 99999,
                buildType = BuildTypeLocator(id = "buildConfigurationId"),
                property = listOf(
                    PropertyLocator(
                        name = "fieldName",
                        value = "fieldValue",
                        matchType = PropertyLocator.MatchType.EQUALS,
                        ignoreCase = true,
                    ),
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testVcsRootsLocator() {
        val url = "https://github.com/github/training-kit.git"
        val expected =
            "count:99999,property:(ignoreCase:true,matchType:equals,name:url,value:$url),property:(ignoreCase:true,matchType:equals,name:branch,value:main)"
        val actual = locatorExpander.expand(
            VcsRootInstanceLocator(
                count = 99999,
                property = listOf(
                    PropertyLocator(
                        name = "url",
                        value = url,
                        matchType = PropertyLocator.MatchType.EQUALS,
                        ignoreCase = true,
                    ),
                    PropertyLocator(
                        name = "branch",
                        value = "main",
                        matchType = PropertyLocator.MatchType.EQUALS,
                        ignoreCase = true,
                    ),
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testProjectLocator() {
        val expected = "count:2000,parameter:(name:ParameterName,value:ParameterValue)"
        val actual = locatorExpander.expand(
            ProjectLocator(
                count = 2000,
                parameter = listOf(
                    PropertyLocator(
                        name = "ParameterName",
                        value = "ParameterValue",
                    ),
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleProjectLocator() {
        val expected = "id:TestProjectId"
        val actual = locatorExpander.expand(
            ProjectLocator(
                id = "TestProjectId",
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testNestedProjectLocator() {
        val expected = "parentProject:(id:TestParentProjectId)"
        val actual = locatorExpander.expand(
            ProjectLocator(
                parentProject = ProjectLocator(
                    id = "TestParentProjectId",
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleBuildTypeLocator() {
        val expected = "id:TestBuildTypeId"
        val actual = locatorExpander.expand(
            BuildTypeLocator(
                id = "TestBuildTypeId",
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testBuildLocator() {
        val expected =
            "branch:default:any,buildType:(template:(id:WlValidator)),count:100,state:finished,status:FAILURE"
        val actual = locatorExpander.expand(
            BuildLocator(
                buildType = BuildTypeLocator(template = TemplateLocator(id = "WlValidator")),
                status = "FAILURE",
                state = "finished",
                branch = "default:any",
                count = 100,
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testBuildLocatorWithPagination() {
        val expected = "count:50,start:100"
        val actual = locatorExpander.expand(
            BuildLocator(
                count = 50,
                start = 100,
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testBuildLocatorWithLookupLimit() {
        val expected = "count:2,lookupLimit:1"
        val actual = locatorExpander.expand(
            BuildLocator(
                count = 2,
                lookupLimit = 1,
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testBuildTypeLocatorWithTemplate() {
        val expected = "template:(id:WlValidator)"
        val actual = locatorExpander.expand(
            BuildTypeLocator(
                template = TemplateLocator(id = "WlValidator"),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testInvestigationLocator() {
        val expected = "buildType:(id:TestBuildTypeId)"
        val actual = locatorExpander.expand(
            InvestigationLocator(
                BuildTypeLocator(
                    id = "TestBuildTypeId",
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testProjectLocatorWithPagination() {
        val expected = "count:50,start:100"
        val actual = locatorExpander.expand(
            ProjectLocator(
                count = 50,
                start = 100,
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testProjectLocatorWithAffectedProject() {
        val expected = "affectedProject:(id:TestRootProjectId)"
        val actual = locatorExpander.expand(
            ProjectLocator(
                affectedProject = ProjectLocator(
                    id = "TestRootProjectId",
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testProjectLocatorWithNameOnlyParameter() {
        val expected = "parameter:(name:COMPONENT_NAME)"
        val actual = locatorExpander.expand(
            ProjectLocator(
                parameter = listOf(
                    PropertyLocator(name = "COMPONENT_NAME"),
                ),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testPropertyLocatorValueWithUrlSpecialCharsIsEscaped() {
        // Values that contain URL/query-string delimiters (& ? # + space %) must be
        // percent-encoded so that the locator survives transport with `@Param(encoded = true)`.
        // TC locator structural chars (: , ( )) must remain raw in the output.
        val actual = locatorExpander.expand(
            ProjectLocator(
                parameter = listOf(
                    PropertyLocator(
                        name = "url",
                        value = "https://host/repo.git?x=1&y=2#frag with+plus 100%",
                    ),
                ),
            ),
        )
        val expected = "parameter:(name:url,value:https://host/repo.git%3Fx=1%26y=2%23frag%20with%2Bplus%20100%25)"
        assertEquals(expected, actual)
    }

    @Test
    fun testParseLocatorAndFieldsDecodesPercentEncodedFieldsLikeTeamcity2022() {
        val href = "/app/rest/latest/buildTypes?fields=nextHref%2CbuildType%28id%2Cname%29&locator=count:1,start:1,template:(id:T)"
        val (locator, fields) = parseLocatorAndFields(href, fallbackFields = "unused")
        assertEquals("count:1,start:1,template:(id:T)", locator)
        assertEquals("nextHref,buildType(id,name)", fields)
    }

    @Test
    fun testParseLocatorAndFieldsLeavesAlreadyRawFieldsUnchangedLikeTeamcity2026() {
        val href = "/app/rest/latest/buildTypes?fields=nextHref,buildType(id,name)&locator=count:1,start:1,template:(id:T)"
        val (locator, fields) = parseLocatorAndFields(href, fallbackFields = "unused")
        assertEquals("count:1,start:1,template:(id:T)", locator)
        assertEquals("nextHref,buildType(id,name)", fields)
    }

    @Test
    fun testParseLocatorAndFieldsKeepsLocatorRawEvenIfItLooksPercentEncoded() {
        // `locator` is never percent-encoded by TeamCity, so it must never be decoded either -
        // a `%` that happens to appear in it (e.g. inside an id) is not an encoding artifact.
        val href = "/app/rest/latest/buildTypes?fields=id&locator=count:1,name:100%25done"
        val (locator, fields) = parseLocatorAndFields(href, fallbackFields = "unused")
        assertEquals("count:1,name:100%25done", locator)
        assertEquals("id", fields)
    }

    @Test
    fun testParseLocatorAndFieldsPreservesLiteralPlusInFields() {
        // URLDecoder.decode alone would turn a literal '+' into a space (form-encoding
        // semantics) - wrong for a percent-encoded query value, where '+' must stay '+'.
        val href = "/app/rest/latest/buildTypes?fields=name%3Aa%2Bb&locator=count:1"
        val (_, fields) = parseLocatorAndFields(href, fallbackFields = "unused")
        assertEquals("name:a+b", fields)
    }

    @Test
    fun testParseLocatorAndFieldsFallsBackToCallerFieldsWhenMissingFromHref() {
        val href = "/app/rest/latest/buildTypes?locator=count:1,start:1"
        val (locator, fields) = parseLocatorAndFields(href, fallbackFields = "nextHref,buildType(id)")
        assertEquals("count:1,start:1", locator)
        assertEquals("nextHref,buildType(id)", fields)
    }

    @Test
    fun testParseLocatorAndFieldsSkipsAMalformedQuerySegmentInsteadOfThrowing() {
        val href = "/app/rest/latest/buildTypes?someFlag&fields=id&locator=count:1"
        val (locator, fields) = parseLocatorAndFields(href, fallbackFields = "unused")
        assertEquals("count:1", locator)
        assertEquals("id", fields)
    }

    @Test
    fun testParseLocatorAndFieldsThrowsWhenLocatorIsMissing() {
        val href = "/app/rest/latest/buildTypes?fields=id"
        assertThrows(NoSuchElementException::class.java) {
            parseLocatorAndFields(href, fallbackFields = "unused")
        }
    }
}
