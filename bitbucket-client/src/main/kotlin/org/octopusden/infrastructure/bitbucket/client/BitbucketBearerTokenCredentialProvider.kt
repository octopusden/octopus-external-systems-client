package org.octopusden.infrastructure.bitbucket.client

class BitbucketBearerTokenCredentialProvider(token: String) :
    BitbucketCredentialProvider({ BitbucketBearerTokenRequestInterceptor(token) })
