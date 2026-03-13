package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

class BuildLocator(
    val buildType: BuildTypeLocator? = null,
    val status: String? = null,
    val state: String? = null,
    val branch: String? = null,
    val running: Boolean? = null,
    val count: Int? = null,
) : BaseLocator()
