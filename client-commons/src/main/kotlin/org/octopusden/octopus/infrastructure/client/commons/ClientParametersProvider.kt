package org.octopusden.octopus.infrastructure.client.commons

interface ClientParametersProvider {
    fun getApiUrl(): String
    fun getAuth(): CredentialProvider
}
