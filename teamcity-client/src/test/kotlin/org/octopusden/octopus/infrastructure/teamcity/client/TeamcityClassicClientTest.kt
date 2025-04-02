package org.octopusden.octopus.infrastructure.teamcity.client

import it.skrape.core.htmlDocument
import it.skrape.matchers.toBe
import it.skrape.selects.html5.textarea
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityAgentRequirement
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityBuildTypes
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRootEntry
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkFeature
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperties
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperty
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependency
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityStep
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.BuildTypeLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.ProjectLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.PropertyLocator
import org.octopusden.octopus.infrastructure.teamcity.client.dto.locator.VcsRootLocator


private const val HOST = "localhost:8111"
private const val USER = "admin"
private const val PASSWORD = "admin"

class TeamcityClassicClientTest {

    private val client by lazy {
        TeamcityClassicClient(
            object : ClientParametersProvider {
                override fun getApiUrl() = "http://$HOST"
                override fun getAuth() = StandardBasicCredCredentialProvider(USER, PASSWORD)
            }
        )
    }

    private fun createProject(projectName: String, parentId: String = "RDDepartment") =
        client.createProject(
            TeamcityCreateProject(
                name = projectName,
                parentProject = TeamcityLinkProject(id = parentId)
            )
        )

    private fun createBuildType(buildName: String, projectId: String) =
        client.createBuildType(
            TeamcityCreateBuildType(
                name = buildName,
                project = TeamcityLinkProject(id = projectId)
            )
        )

    @Test
    fun testServer() {
        assertEquals("2022.04.7 (build 109063)", client.getServer().version)
    }

    @Test
    fun testProject() {
        val project = createProject("TestCreateProject")
        assertEquals(project, client.getProject(project.id))
        assertEquals(project, client.getProject(ProjectLocator(name = project.name)))
        val subProject = createProject("SubProject", project.id)
        assertEquals(subProject.parentProjectId, project.id)
        assertEquals(subProject.parentProject?.name, project.name)
        client.deleteProject(project.id)
    }

    @Test
    fun getBuildTypes() {
        assertEquals(client.getBuildTypes(), TeamcityBuildTypes())
    }

    @Test
    fun createBuildType() {
        val project = createProject("TestCreateBuildType")
        val buildType = createBuildType("TestCreateBuildType", project.id)
        client.setBuildCounter(buildType.id, "21")
        assertEquals(buildType.name, "TestCreateBuildType")
        client.deleteProject(project.id)
    }

    @Test
    fun testBuildTypeFeature() {
        val project = createProject("TestBuildTypeFeature")
        val buildType = createBuildType("TestBuildTypeFeature", project.id)
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

    @Test
    fun testSnapshotDependencies() {
        val project = createProject("TestSnapshotDependencies")
        val sourceBuildType = createBuildType("SourceBuild", project.id)
        val buildType = createBuildType("TestSnapshotDependencies", project.id)
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

    @Test
    fun testBuildSteps() {
        val project = createProject("TestBuildSteps")
        val buildType = createBuildType("TestBuildSteps", project.id)
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

    @Test
    fun testBuildVcsRoots() {
        val project = createProject("TestBuildVcsRoots")
        val buildType = createBuildType("TestBuildVcsRoots", project.id)
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

    @Test
    fun testTemplates() {
        val project = createProject("TestTemplates")
        val buildType = createBuildType("TestTemplates", project.id)
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

    @Test
    fun testProjectBuildTypes() {
        val project = createProject("TestProjectBuildTypes")
        val buildType = client.createBuildType(project.id, "ProjectBuildType")
        assertEquals(buildType.name, client.getBuildTypes(project.id).buildTypes.first().name)
        client.deleteProject(project.id)
    }

    @Test
    fun testBuildTypesAgentRequirements() {
        val project = createProject("TestProjectBuildTypes")
        val buildType = client.createBuildType(project.id, "ProjectBuildType")
        val properties = TeamcityProperties(
            listOf(
                TeamcityProperty("property-name", "property-value")
            )
        )
        val requirement = client.addAgentRequirementToBuildType(BuildTypeLocator(id = buildType.id), null,  TeamcityAgentRequirement(
            id = "requirement",
            type = "matches",
            properties = properties,
            name = "requirement",
            disabled = false,
            inherited = false,
            href = ""
        ))
        assertEquals(requirement.name, client.getAllAgentRequirements(buildType.id).agentRequirements.first().name)
        client.deleteProject(project.id)
    }

    @Test
    fun testParameters() {
        val project = createProject("TestParameters")
        val buildType = createBuildType("TestParameters", project.id)
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

    @Test
    fun testProjectLocator() {
        val project = createProject("TestProjectLocator")
        val secondProject = createProject("AnotherTestProjectLocator")
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

    @Test
    fun testUploadMetarunner() {
        val projectId = "RDDepartment"
        val metarunnerId = "TestMetarunner"
        val testMetarunnerName = "$metarunnerId.xml"
        val check = { metarunnerContent: String ->
            htmlDocument(
                HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder()
                        .uri(URI("http://$HOST/admin/editProject.html?projectId=$projectId&tab=metaRunner&editRunnerId=$metarunnerId"))
                        .header("Origin", "http://$HOST").header("Authorization", "Basic YWRtaW46YWRtaW4=")
                        .method("GET", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString()
                ).body()
            ) {
                textarea {
                    withId = "metaRunnerContent"
                    findAll {
                        size toBe 1
                        this[0].text toBe metarunnerContent
                    }
                }
            }
        }
        val testMetarunnerCreateContent = TeamcityClassicClientTest::class.java.classLoader
            .getResourceAsStream("${metarunnerId}Create.xml")!!.readBytes()
        client.uploadMetarunner(projectId, testMetarunnerName, testMetarunnerCreateContent)
        check.invoke(String(testMetarunnerCreateContent))
        val testMetarunnerEditContent = TeamcityClassicClientTest::class.java.classLoader
            .getResourceAsStream("${metarunnerId}Edit.xml")!!.readBytes()
        client.uploadMetarunner(projectId, testMetarunnerName, testMetarunnerEditContent)
        check.invoke(String(testMetarunnerEditContent))
    }
}