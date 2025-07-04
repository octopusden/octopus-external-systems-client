package org.octopusden.octopus.infrastructure.teamcity.client

import org.junit.jupiter.api.Assertions.assertEquals

class TeamcityClassicClientV25Test: BaseTeamcityClientTest() {

    override val host = "localhost:8112"

    override fun serverTest() {
        assertEquals("2025.03.3 (build 186370)", client.getServer().version)
    }

    override fun uploadTest() {
        val projectId = "RDDepartment"
        val recipeId = "TestMetarunner"
        val recipeName = "$recipeId.xml"

        val testRecipeCreateContent = TeamcityClassicClientTest::class.java.classLoader
            .getResourceAsStream("${recipeId}Create.xml")!!.readBytes()
        client.uploadRecipe(projectId, recipeName, testRecipeCreateContent)
        checkHtmlContent(host, "http://$host/admin/editProject.html?projectId=$projectId&tab=recipe&editRecipeId=$recipeId", "recipeContent", String(testRecipeCreateContent))

        val testRecipeEditContent = TeamcityClassicClientTest::class.java.classLoader
            .getResourceAsStream("${recipeId}Edit.xml")!!.readBytes()
        client.uploadRecipe(projectId, recipeName, testRecipeEditContent)
        checkHtmlContent(host, "http://$host/admin/editProject.html?projectId=$projectId&tab=recipe&editRecipeId=$recipeId", "recipeContent", String(testRecipeEditContent))
    }

}