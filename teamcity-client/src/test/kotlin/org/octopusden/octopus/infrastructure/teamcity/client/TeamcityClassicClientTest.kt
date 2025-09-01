package org.octopusden.octopus.infrastructure.teamcity.client

import it.skrape.core.htmlDocument
import it.skrape.matchers.toBe
import it.skrape.selects.html5.textarea
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityAgentRequirement
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityBuildTypes
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateQueuedBuild
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRootEntry
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkFeature
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperties
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperty
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityQueuedBuild
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependency
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityStep
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.AgentRequirementLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildTypeLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.ProjectLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.PropertyLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootInstanceLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootLocator

class TeamcityClassicClientTest {
    companion object {
        const val USER = "admin"
        const val PASSWORD = "admin"

        @JvmStatic
        fun teamcityConfigurations(): List<TeamcityTestConfiguration> = listOf(
            TeamcityTestConfiguration(
                name = "v22",
                host = "localhost:8111",
                version = "2022.04.7 (build 109063)"
            ),
            TeamcityTestConfiguration(
                name = "v25",
                host = "localhost:8112",
                version = "2025.03.3 (build 186370)"
            )
        )

        @JvmStatic
        fun teamcityContexts(): List<TeamcityTestConfiguration> =
            teamcityConfigurations().map { TeamcityTestConfiguration(it.name, it.host, it.version) }
    }

    private fun createClient(config: TeamcityTestConfiguration): TeamcityClassicClient {
        return TeamcityClassicClient(
            object : ClientParametersProvider {
                override fun getApiUrl() = "http://${config.host}"
                override fun getAuth() = StandardBasicCredCredentialProvider(USER, PASSWORD)
            }
        )
    }

    private fun createProject(client: TeamcityClient, projectName: String, parentId: String = "RDDepartment") =
        client.createProject(
            TeamcityCreateProject(
                name = projectName,
                parentProject = TeamcityLinkProject(id = parentId)
            )
        )

    private fun createBuildType(client: TeamcityClient, buildName: String, projectId: String) =
        client.createBuildType(
            TeamcityCreateBuildType(
                name = buildName,
                project = TeamcityLinkProject(id = projectId)
            )
        )

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testServer(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        assertEquals(config.version, client.getServer().version)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testProject(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestCreateProject")
        assertEquals(project, client.getProject(project.id))
        assertEquals(project, client.getProject(ProjectLocator(name = project.name)))
        val subProject = createProject(client, "SubProject", project.id)
        assertEquals(subProject.parentProjectId, project.id)
        assertEquals(subProject.parentProject?.name, project.name)
        client.deleteProject(project.id)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun getBuildTypes(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        assertEquals(client.getBuildTypes(), TeamcityBuildTypes())
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun createBuildType(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestCreateBuildType")
        val buildType = createBuildType(client, "TestCreateBuildType", project.id)
        client.setBuildCounter(buildType.id, "21")
        assertEquals(buildType.name, "TestCreateBuildType")
        client.deleteProject(project.id)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testBuildTypeFeature(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestBuildTypeFeature")
        val buildType = createBuildType(client, "TestBuildTypeFeature", project.id)
        client.addBuildTypeFeature(
            buildType.id, TeamcityLinkFeature(
                type = "VcsLabeling",
                id = "VcsLabeling",
                properties = TeamcityProperties(
                    listOf(
                        TeamcityProperty("labelingPattern", "%LABELING_PATTERN%"),
                        TeamcityProperty("successfulOnly", "true"),
                        TeamcityProperty("vcsRootId", "vcsId"),
                    )
                )
            )
        )
        val features = client.getBuildTypeFeatures(buildType.id).features
        assertEquals(1, features.size)
        val feature = features.first()
        assertEquals("VcsLabeling", feature.type)
        val featureId = feature.id
        fun getVcsRootId() = client.getBuildTypeFeatureParameter(buildType.id, featureId, "vcsRootId")
        assertEquals("vcsId", getVcsRootId())
        client.updateBuildTypeFeatureParameter(buildType.id, featureId, "vcsRootId", "newVcsId")
        assertEquals("newVcsId", getVcsRootId())
        client.deleteProject(project.id)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testSnapshotDependencies(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestSnapshotDependencies")
        val sourceBuildType = createBuildType(client, "SourceBuild", project.id)
        val buildType = createBuildType(client, "TestSnapshotDependencies", project.id)
        client.createSnapshotDependency(
            buildType.id,
            TeamcitySnapshotDependency(
                id = sourceBuildType.name,
                type = "snapshot_dependency",
                properties = TeamcityProperties(
                    listOf(
                        TeamcityProperty("run-build-if-dependency-failed", "MAKE_FAILED_TO_START"),
                        TeamcityProperty("run-build-if-dependency-failed-to-start", "MAKE_FAILED_TO_START"),
                        TeamcityProperty("run-build-on-the-same-agent", "false"),
                        TeamcityProperty("take-started-build-with-same-revisions", "true"),
                        TeamcityProperty("take-successful-builds-only", "true"),
                    )
                ),
                sourceBuildType = TeamcityLinkBuildType(sourceBuildType.id)
            )
        )
        val dependency = client.getSnapshotDependencies(buildType.id).snapshotDependencies.first()
        client.deleteSnapshotDependency(buildType.id, dependency.id)
        client.deleteProject(project.id)
        assertNotEquals(sourceBuildType.name, dependency.id)
        assertEquals(sourceBuildType.id, dependency.id)
        assertEquals(sourceBuildType.id, dependency.sourceBuildType.id)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testBuildSteps(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestBuildSteps")
        val buildType = createBuildType(client, "TestBuildSteps", project.id)
        val step = TeamcityStep(
            id = "RUNNER_1",
            name = "cmd",
            type = "simpleRunner",
            disabled = false,
            properties = TeamcityProperties(
                listOf(
                    TeamcityProperty("script.content", "echo 1"),
                    TeamcityProperty("teamcity.step.mode", "default"),
                    TeamcityProperty("use.custom.script", "true"),
                )
            )
        )
        client.createBuildStep(
            buildType.id,
            step
        )
        client.disableBuildStep(buildType.id, "RUNNER_1", true)
        val steps = client.getBuildSteps(buildType.id).steps
        client.deleteProject(project.id)
        assertEquals(true, steps.first().disabled)
        assertEquals(step.name, steps.first().name)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testBuildVcsRoots(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestBuildVcsRoots")
        val buildType = createBuildType(client, "TestBuildVcsRoots", project.id)
        val url = "ssh://git@github.com:octopusden/octopus-external-systems-client.git"
        val vcsRoot = client.createVcsRoot(
            TeamcityCreateVcsRoot(
                name = "${project.name}_VCS_ROOT",
                vcsName = TeamcityVCSType.GIT.value,
                projectLocator = project.id,
                properties = TeamcityProperties(
                    listOf(
                        TeamcityProperty("url", url),
                        TeamcityProperty("branch", "master"),
                        TeamcityProperty("authMethod", "PRIVATE_KEY_DEFAULT"),
                        TeamcityProperty("userForTags", "tcagent"),
                        TeamcityProperty("username", "git"),
                        TeamcityProperty("ignoreKnownHosts", "true"),
                    )
                )
            )
        )
        client.createBuildTypeVcsRootEntry(
            buildType.id,
            TeamcityCreateVcsRootEntry(
                id = vcsRoot.id,
                vcsRoot = TeamcityLinkVcsRoot(vcsRoot.id)
            )
        )
        val btVcsRootEntry = client.getBuildTypeVcsRootEntries(buildType.id).entries.first()
        val btVcsRoot = client.getBuildTypeVcsRootEntry(buildType.id, vcsRoot.id)
        val tcVcsRoot = client.getVcsRoot(vcsRoot.id)

        assertEquals(url, client.getVcsRootProperty(tcVcsRoot.id, "url"))
        val vcsRootsByLocator = client.getVcsRoots(
            VcsRootLocator(
                property = listOf(
                    PropertyLocator("url", url)
                )
            )
        ).vcsRoots
        assertEquals(1, vcsRootsByLocator.size)
        assertEquals(tcVcsRoot.id, vcsRootsByLocator.first().id)
        val newUrl = "ssh://git@github.com:octopusden/octopus-teamcity-automation.git"
        client.updateVcsRootProperty(tcVcsRoot.id, "url", newUrl)
        assertEquals(newUrl, client.getVcsRootProperty(tcVcsRoot.id, "url"))
        client.deleteProject(project.id)
        assertEquals("${project.name}_VCS_ROOT", tcVcsRoot.name)
        assertEquals(btVcsRootEntry.vcsRoot.href, btVcsRoot.vcsRoot.href)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testTemplates(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestTemplates")
        val buildType = createBuildType(client, "TestTemplates", project.id)
        val template = client.createBuildType(
            TeamcityCreateBuildType(
                name = "Template",
                project = TeamcityLinkProject(id = project.id),
                templateFlag = true
            )
        )
        client.attachTemplateToBuildType(buildType.id, template.id)
        var modifiedBuildType = client.getBuildType(buildType.id)
        assertEquals(1, modifiedBuildType.templates!!.buildTypes.size)
        assertEquals("Template", modifiedBuildType.templates!!.buildTypes.first().name)
        client.detachTemplatesFromBuildType(buildType.id)
        modifiedBuildType = client.getBuildType(buildType.id)
        assertEquals(0, modifiedBuildType.templates!!.buildTypes.size)
        client.deleteProject(project.id)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testProjectBuildTypes(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestProjectBuildTypes")
        val buildType = client.createBuildType(project.id, "ProjectBuildType")
        assertEquals(buildType.name, client.getBuildTypes(project.id).buildTypes.first().name)
        client.deleteProject(project.id)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testBuildTypesAgentRequirements(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestProjectBuildTypes")
        val buildType = client.createBuildType(project.id, "ProjectBuildType")
        val properties = TeamcityProperties(
            listOf(
                TeamcityProperty("property-name", "property-value")
            )
        )
        val requirement = client.addAgentRequirementToBuildType(
            BuildTypeLocator(id = buildType.id),  TeamcityAgentRequirement(
                id = null,
                type = "matches",
                properties = properties,
                name = "requirementName",
                disabled = false,
                inherited = false,
                href = ""
            )
        )
        val actualResponse = client.getAgentRequirements(buildType.id)
        assertEquals(1, actualResponse.count)
        val actual = actualResponse.agentRequirements.first()
        assertEquals(requirement.id, actual.id)
        assertEquals("matches", actual.type)
        assertIterableEquals(properties.properties, actual.properties.properties)
        assertEquals(requirement, actual)
        client.deleteAgentRequirement(BuildTypeLocator(id = buildType.id), AgentRequirementLocator(id = requirement.id))
        client.deleteProject(project.id)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testParameters(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestParameters")
        val buildType = createBuildType(client, "TestParameters", project.id)
        listOf(
            Pair(ConfigurationType.PROJECT, project.id),
            Pair(ConfigurationType.BUILD_TYPE, buildType.id)
        ).forEach { (type, id) ->
            client.createParameter(type, id, "empty_parameter")
            client.setParameter(type, id, "empty_parameter", "123")
            assertEquals("123", client.getParameter(type, id, "empty_parameter"))
            client.createParameter(type, id, "not_empty_parameter", "sun")
            assertEquals("sun", client.getParameter(type, id, "not_empty_parameter"))
            client.createParameter(type, id, TeamcityProperty("parameter", "someone"))
            assertEquals("someone", client.getParameter(type, id, "parameter"))
            client.setParameter(type, id, "parameter", "other")
            assertEquals("other", client.getParameter(type, id, "parameter"))
            client.deleteParameter(type, id, "parameter")
        }
        client.deleteProject(project.id)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testProjectLocator(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestProjectLocator")
        val secondProject = createProject(client, "AnotherTestProjectLocator")
        client.createParameter(ConfigurationType.PROJECT, project.id, "ParameterName", "ParameterValue")
        val projects = client.getProjects(
            ProjectLocator(
                count = 2000,
                parameter = listOf(
                    PropertyLocator(
                        name = "ParameterName",
                        value = "ParameterValue"
                    )
                )
            )
        ).projects
        assertEquals(1, projects.size)
        assertEquals("TestProjectLocator", projects.first().name)
        client.deleteProject(secondProject.id)
        client.deleteProject(project.id)
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testUploadMetarunner(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val projectId = "RDDepartment"
        val metarunnerId = "TestMetarunner"
        val metarunnerName = "$metarunnerId.xml"

        val (tabName, editQueryId, textAreaId) = when {
            config.version.startsWith("2025") -> Triple("recipe", "editRecipeId", "recipeContent")
            else -> Triple("metaRunner", "editRunnerId", "metaRunnerContent")
        }

        val testCreateContent = TeamcityClassicClientTest::class.java.classLoader
            .getResourceAsStream("${metarunnerId}Create.xml")!!.readBytes()
        client.uploadMetarunner(projectId, metarunnerName, testCreateContent)
        checkHtmlContent("http://${config.host}/admin/editProject.html?projectId=$projectId&tab=$tabName&$editQueryId=$metarunnerId", textAreaId, String(testCreateContent))

        val testEditContent = TeamcityClassicClientTest::class.java.classLoader
            .getResourceAsStream("${metarunnerId}Edit.xml")!!.readBytes()
        client.uploadMetarunner(projectId, metarunnerName, testEditContent)
        checkHtmlContent( "http://${config.host}/admin/editProject.html?projectId=$projectId&tab=$tabName&$editQueryId=$metarunnerId", textAreaId, String(testEditContent))
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testQueueBuild(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestQueueBuild")
        try {
            val buildType = createBuildType(client, "TestQueueBuildType", project.id)
            val request = TeamcityCreateQueuedBuild(
                buildType = BuildTypeLocator(id = buildType.id),
                branchName = "master"
            )
            val queued = client.queueBuild(request)
            assertNotNull(queued.id)
            assertEquals("queued", queued.state)
        } finally {
            client.deleteProject(project.id)
        }
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testGetProjectsWithLocatorAndFields(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "testGetProjectsWithFields")
        try {
            val subProject = createProject(client, "SubProject_WithFields", project.id)
            val buildType = createBuildType(client, "testGetProjectsWithFieldsBuildType", project.id)
            val fields = "project(id,name,webUrl,archived,href," +
                    "buildTypes(buildType(id,name,projectId,projectName,href,template,vcs-root-entries))," +
                    "projects(project(id,name,webUrl,archived,href)))"
            val locator = ProjectLocator(name = project.name)
            val actualProject = client.getProjectsWithLocatorAndFields(locator, fields).projects.first()
            val expectedProject = client.getProject(project.id)

            assertEquals(expectedProject.id, actualProject.id)
            assertEquals(expectedProject.name, actualProject.name)

            val actualBuildTypes = actualProject.buildTypes
            assertNotNull(actualBuildTypes)
            assertEquals(expectedProject.buildTypes!!.buildTypes.size, actualBuildTypes!!.buildTypes.size)
            assertEquals(buildType.id, actualBuildTypes.buildTypes.first().id)

            val actualInnerProjects = actualProject.projects
            assertNotNull(actualInnerProjects)
            assertEquals(expectedProject.projects!!.projects.size, actualInnerProjects!!.projects.size)
            assertEquals(subProject.id, actualInnerProjects.projects.first().id)
        } finally {
            client.deleteProject(project.id)
        }
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testGetBuildTypesWithVcsRootInstanceLocatorAndFields(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestGetBuildTypesWithVcsRootInstanceLocatorAndFields")
        try {
            val buildType = createBuildType(client, "TestGetBuildTypesWithVcsRootInstanceLocatorAndFieldsBuildType", project.id)
            val url = "ssh://git@github.com:octopusden/octopus-external-systems-client.git"
            val vcsRoot = client.createVcsRoot(
                TeamcityCreateVcsRoot(
                    name = "${project.name}_VCS_ROOT",
                    vcsName = TeamcityVCSType.GIT.value,
                    projectLocator = project.id,
                    properties = TeamcityProperties(
                        listOf(
                            TeamcityProperty("url", url),
                            TeamcityProperty("branch", "master"),
                            TeamcityProperty("authMethod", "PRIVATE_KEY_DEFAULT"),
                            TeamcityProperty("userForTags", "tcagent"),
                            TeamcityProperty("username", "git"),
                            TeamcityProperty("ignoreKnownHosts", "true")
                        )
                    )
                )
            )
            client.createBuildTypeVcsRootEntry(
                buildType.id,
                TeamcityCreateVcsRootEntry(
                    id = vcsRoot.id,
                    vcsRoot = TeamcityLinkVcsRoot(vcsRoot.id)
                )
            )
            val locator = VcsRootInstanceLocator(
                property = listOf(PropertyLocator("url", url, PropertyLocator.MatchType.EQUALS, ignoreCase = true)),
                count = 2000
            )
            val fields = "buildType(id,name,projectId,projectName,href)"
            val result = client.getBuildTypesWithVcsRootInstanceLocatorAndFields(locator, fields)
            val found = result.buildTypes
            assertNotNull(found)
            assertEquals(1, found.size)
            val actual = found.first()
            assertEquals(buildType.id, actual.id)
            assertEquals(buildType.name, actual.name)

        } finally {
            client.deleteProject(project.id)
        }
    }

    @ParameterizedTest
    @MethodSource("teamcityContexts")
    fun testGetVcsRootInstances(config: TeamcityTestConfiguration) {
        val client = createClient(config)
        val project = createProject(client, "TestGetVcsRootInstances")
        try {
            val buildType = createBuildType(client, "TestGetVcsRootInstancesBuildType", project.id)
            val url = "ssh://git@github.com:octopusden/octopus-external-systems-client.git"
            val vcsRoot = client.createVcsRoot(
                TeamcityCreateVcsRoot(
                    name = "${project.name}_VCS_ROOT",
                    vcsName = TeamcityVCSType.GIT.value,
                    projectLocator = project.id,
                    properties = TeamcityProperties(
                        listOf(
                            TeamcityProperty("url", url),
                            TeamcityProperty("branch", "master"),
                            TeamcityProperty("authMethod", "PRIVATE_KEY_DEFAULT"),
                            TeamcityProperty("userForTags", "tcagent"),
                            TeamcityProperty("username", "git"),
                            TeamcityProperty("ignoreKnownHosts", "true")
                        )
                    )
                )
            )
            client.createBuildTypeVcsRootEntry(
                buildTypeId = buildType.id,
                vcsRootEntry = TeamcityCreateVcsRootEntry(
                    id = vcsRoot.id,
                    vcsRoot = TeamcityLinkVcsRoot(vcsRoot.id)
                )
            )
            val locator = VcsRootInstanceLocator(property = listOf(PropertyLocator("url", url, PropertyLocator.MatchType.EQUALS, ignoreCase = true)))
            val instances = client.getVcsRootInstances(locator).vcsRootInstances
            assertEquals(vcsRoot.id, instances.first().vcsRootId)
        } finally {
            client.deleteProject(project.id)
        }
    }

    private fun checkHtmlContent(
        url: String,
        textareaId: String,
        expectedContent: String,
    ) {
        val responseBody = HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder()
                    .uri(URI(url))
                    .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                    .GET()
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            ).body()

        htmlDocument(responseBody) {
            textarea {
                withId = textareaId
                findAll {
                    size toBe 1
                    this[0].text toBe expectedContent
                }
            }
        }
    }
}