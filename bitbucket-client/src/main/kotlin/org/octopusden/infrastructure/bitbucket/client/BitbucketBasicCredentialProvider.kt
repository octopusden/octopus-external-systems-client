package org.octopusden.infrastructure.bitbucket.client

class BitbucketBasicCredentialProvider(username: String, password: String) :
    BitbucketCredentialProvider({ BitbucketBasicCredTokenRequestInterceptor(username, password) })
