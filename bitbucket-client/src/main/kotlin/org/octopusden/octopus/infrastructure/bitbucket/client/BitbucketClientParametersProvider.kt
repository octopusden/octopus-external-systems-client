package org.octopusden.octopus.infrastructure.bitbucket.client

import org.octopusden.octopus.infrastructure.client.commons.ClientParametersProvider

interface BitbucketClientParametersProvider : ClientParametersProvider {
    override fun getAuth(): BitbucketCredentialProvider
}
