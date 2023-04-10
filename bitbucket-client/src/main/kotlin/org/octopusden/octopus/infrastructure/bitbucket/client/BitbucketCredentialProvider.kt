package org.octopusden.octopus.infrastructure.bitbucket.client

import org.octopusden.octopus.infrastructure.client.commons.CredentialProvider
import feign.RequestInterceptor

abstract class BitbucketCredentialProvider(func: () -> RequestInterceptor) : CredentialProvider(func)
