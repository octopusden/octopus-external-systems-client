package org.octopusden.octopus.infrastructure.teamcity.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityBuildTypes
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateVcsRootEntry
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkFeature
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkVcsRoot
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperties
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperty
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcitySnapshotDependency
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityStep


private const val HOST = "localhost:8112"
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

    private fun createProject(projectName: String, parentId: String = "RDDepartment"): TeamcityProject {
        val id = projectName + "Id"
        client.createProject(
            TeamcityCreateProject(
                name = projectName,
                id = id,
                parentProject = TeamcityLinkProject(id = parentId)
            )
        )
        return client.getProject(id)
    }

    private fun createBuildType(buildName: String, projectId: String): TeamcityBuildType {
        val id = "${buildName}Id"
        client.createBuildType(
            TeamcityCreateBuildType(
                id = id,
                name = buildName,
                project = TeamcityLinkProject(id = projectId)
            )
        )
        return client.getBuildType(id)
    }

    @Test
    fun testProject() {
        val project = createProject("TestCreateProject")
        assertEquals(project, client.getProject("id:${project.id}"))
        assertEquals(project, client.getProject("name:${project.name}"))
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
        client.updateBuildCounter(buildType.id, "21")
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
        fun getVcsRootId() = client.getBuildTypeFeatureParameter(buildType.id, featureId, "vcsRootId")//.toString()
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
        val dependency = client.getSnapshotDependencies(buildType.id).snapshotDependency.first()
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
                vcsName = "jetbrains.git",
                projectLocator = project.id,
                properties = TeamcityProperties(
                    listOf(
                        TeamcityProperty("url",url),
                        TeamcityProperty("branch","master"),
                        TeamcityProperty("authMethod","PRIVATE_KEY_DEFAULT"),
                        TeamcityProperty("userForTags","tcagent"),
                        TeamcityProperty("username","git"),
                        TeamcityProperty("ignoreKnownHosts","true"),
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

        assertEquals(url, client.getVcsRootProperty(tcVcsRoot.id,"url"))
        val newUrl = "ssh://git@github.com:octopusden/octopus-teamcity-automation.git"
        client.updateVcsRootProperty(tcVcsRoot.id,"url", newUrl)
        assertEquals(newUrl, client.getVcsRootProperty(tcVcsRoot.id,"url"))
        client.deleteProject(project.id)
        assertEquals("${project.name}_VCS_ROOT", tcVcsRoot.name)
        assertEquals(btVcsRootEntry.vcsRoot.href, btVcsRoot.vcsRoot.href)
//        println("#######################################\n"+tcVcsRootEntry)
    }

    @Test
    fun test() {
        val project = createProject("test")
        val buildType = createBuildType("test", project.id)
        // TODO: test
        client.deleteProject(project.id)
    }

}