package org.octopusden.octopus.infrastructure.client.commons

class StandardBearerTokenCredentialProvider(token: String) :
    CredentialProvider({ StandardBearerTokenRequestInterceptor(token) })
