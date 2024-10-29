package org.octopusden.octopus.infrastructure.artifactory.client.dto

import java.util.Date

data class Tokens(val tokens: List<Token>) {
    data class Token(val subject: String, val issuer: String, val expiry: Date, val description: String?)
}
