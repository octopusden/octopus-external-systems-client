package org.octopusden.octopus.infrastructure.teamcity.client.dto.locator

import org.octopusden.octopus.infrastructure.teamcity.client.TeamcityLocatorExpander

open class BaseLocator{

    override fun toString() = TeamcityLocatorExpander().expand(this)
}
