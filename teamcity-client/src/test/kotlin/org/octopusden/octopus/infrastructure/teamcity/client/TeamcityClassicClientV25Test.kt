package org.octopusden.octopus.infrastructure.teamcity.client

class TeamcityClassicClientV25Test: BaseTeamcityClientTest() {
    override val host = "localhost:8112"
    override val tcVersion: String = "2025.03.3 (build 186370)"
}