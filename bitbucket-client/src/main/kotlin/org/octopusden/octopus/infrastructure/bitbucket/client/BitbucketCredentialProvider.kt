package org.octopusden.octopus.infrastructure.bitbucket.client

import feign.RequestInterceptor
import org.octopusden.octopus.infrastructure.client.commons.CredentialProvider

abstract class BitbucketCredentialProvider(
    func: () -> RequestInterceptor,
) : CredentialProvider(func)
