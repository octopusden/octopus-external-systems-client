package org.octopusden.octopus.infrastructure.teamcity.client

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.Util
import feign.jackson.JacksonDecoder
import java.lang.reflect.Type

class TeamcityClientDecoder(val mapper: ObjectMapper) : JacksonDecoder(mapper) {
    override fun decode(response: Response, type: Type): Any {
        return if (type.typeName == TeamcityString::class.java.typeName
        ) {
            TeamcityString(
                Util.toString(
                    response.body().asReader(response.charset())
                )
            )
        } else {
            super.decode(response, type)
        }
    }
}
