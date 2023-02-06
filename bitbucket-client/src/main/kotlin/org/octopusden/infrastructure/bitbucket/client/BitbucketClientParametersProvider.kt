package org.octopusden.infrastructure.bitbucket.client

import org.octopusden.infrastructure.client.commons.ClientParametersProvider

interface BitbucketClientParametersProvider : ClientParametersProvider {
    override fun getAuth(): BitbucketCredentialProvider
}
