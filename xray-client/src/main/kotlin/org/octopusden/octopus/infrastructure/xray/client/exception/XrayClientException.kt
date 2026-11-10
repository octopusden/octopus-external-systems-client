package org.octopusden.octopus.infrastructure.xray.client.exception

/** Xray answered with an error status; [status] is the HTTP status. */
open class XrayClientException(
    val status: Int,
    message: String,
) : RuntimeException(message)

class XrayNotFoundException(
    message: String,
) : XrayClientException(404, message)
