package org.octopusden.infrastructure.bitbucket.client

import org.octopusden.infrastructure.client.commons.CredentialProvider
import feign.RequestInterceptor

abstract class BitbucketCredentialProvider(func: () -> RequestInterceptor) : CredentialProvider(func)
