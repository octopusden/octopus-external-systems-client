package org.octopusden.octopus.infrastructure.bitbucket.client.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class BitbucketLink(val href: String, val name: BitbucketLinkName)
