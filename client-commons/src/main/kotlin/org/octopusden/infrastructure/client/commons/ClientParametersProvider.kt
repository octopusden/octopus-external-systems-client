package org.octopusden.infrastructure.client.commons

interface ClientParametersProvider {
    fun getApiUrl(): String
    fun getAuth(): CredentialProvider
}
