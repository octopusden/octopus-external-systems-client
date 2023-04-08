package org.octopusden.octopus.infrastructure.bitbucket.client

class BitbucketBearerTokenCredentialProvider(token: String) :
    BitbucketCredentialProvider({ BitbucketBearerTokenRequestInterceptor(token) })
