package org.octopusden.octopus.infrastructure.teamcity.client

import com.fasterxml.jackson.annotation.JsonValue

enum class TeamcityVCSType(@get:JsonValue val value: String) {
    GIT("jetbrains.git"),
    HG("mercurial");
}