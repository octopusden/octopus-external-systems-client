package org.octopusden.octopus.infrastructure.teamcity.client

import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

class AlwaysFailTest {
    @Test
    fun alwaysFails() {
        fail<Unit>("intentional failure")
    }
}
