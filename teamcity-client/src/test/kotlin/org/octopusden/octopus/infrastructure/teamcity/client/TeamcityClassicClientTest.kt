package org.octopusden.octopus.infrastructure.teamcity.client

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider
import org.octopusden.octopus.infrastructure.client.commons.StandardBasicCredCredentialProvider
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityBuildTypes
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateBuildType
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityCreateProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkFeature
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityLinkProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProject
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperties
import org.octopusden.octopus.infrastructure.teamcity.client.dto.TeamcityProperty


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
        fun getVcsRootId() = client.getBuildTypeFeatureParameter(buildType.id, featureId, "vcsRootId").toString()
        assertEquals("vcsId", getVcsRootId())
        client.updateBuildTypeFeatureParameter(buildType.id, featureId, "vcsRootId", "newVcsId")
        assertEquals("newVcsId", getVcsRootId())
        client.deleteProject(project.id)
    }
}