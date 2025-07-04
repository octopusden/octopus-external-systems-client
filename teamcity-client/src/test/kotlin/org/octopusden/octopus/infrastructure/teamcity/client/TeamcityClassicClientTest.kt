package org.octopusden.octopus.infrastructure.teamcity.client

import org.junit.jupiter.api.Assertions.assertEquals

class TeamcityClassicClientTest: BaseTeamcityClientTest() {

    override val host = "localhost:8111"

    override fun serverTest() {
        assertEquals("2022.04.7 (build 109063)", client.getServer().version)
    }

    override fun uploadTest() {
        val projectId = "RDDepartment"
        val metarunnerId = "TestMetarunner"
        val testMetarunnerName = "$metarunnerId.xml"

        val testMetarunnerCreateContent = TeamcityClassicClientTest::class.java.classLoader
            .getResourceAsStream("${metarunnerId}Create.xml")!!.readBytes()
        client.uploadMetarunner(projectId, testMetarunnerName, testMetarunnerCreateContent)
        checkHtmlContent(host, "http://$host/admin/editProject.html?projectId=$projectId&tab=metaRunner&editRunnerId=$metarunnerId", "metaRunnerContent", String(testMetarunnerCreateContent))

        val testMetarunnerEditContent = TeamcityClassicClientTest::class.java.classLoader
            .getResourceAsStream("${metarunnerId}Edit.xml")!!.readBytes()
        client.uploadMetarunner(projectId, testMetarunnerName, testMetarunnerEditContent)
        checkHtmlContent(host, "http://$host/admin/editProject.html?projectId=$projectId&tab=metaRunner&editRunnerId=$metarunnerId", "metaRunnerContent", String(testMetarunnerEditContent))
    }

}