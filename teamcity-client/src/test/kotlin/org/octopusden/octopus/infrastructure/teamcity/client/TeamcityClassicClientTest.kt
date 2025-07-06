package org.octopusden.octopus.infrastructure.teamcity.client

class TeamcityClassicClientTest: BaseTeamcityClientTest() {
    override val host = "localhost:8111"
    override val tcVersion: String = "2022.04.7 (build 109063)"
}