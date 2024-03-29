package org.octopusden.octopus.infrastructure.client.commons

open class StandardBasicCredCredentialProvider(username: String, password: String) :
    CredentialProvider({ StandardBasicCredTokenRequestInterceptor(username, password) })
