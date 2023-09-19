package org.octopusden.octopus.infrastructure.gitea.client

import com.fasterxml.jackson.databind.ObjectMapper
import feign.Response
import feign.jackson.JacksonDecoder
import org.octopusden.octopus.infrastructure.gitea.client.dto.BaseGiteaEntity
import org.octopusden.octopus.infrastructure.gitea.client.dto.GiteaEntityList
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class GiteaClientDecoder(val mapper: ObjectMapper) : JacksonDecoder(mapper) {
    override fun decode(response: Response, type: Type): Any {
        val parameterizedType = type as? ParameterizedType
        return if (parameterizedType
                ?.rawType
                ?.typeName == GiteaEntityList::class.java.typeName
        ) {

            val collection = super.decode(
                response,
                CollectionType(parameterizedType!!.actualTypeArguments)
            ) as Collection<BaseGiteaEntity>

            val hasMore = response.headers()["X-HasMore"]
                ?.firstOrNull()
                ?.toBoolean()

            GiteaEntityList(hasMore, collection)
        } else {
            super.decode(response, type)
        }
    }

    private class CollectionType(private val actualTypeArguments: Array<Type>) : ParameterizedType {
        override fun getActualTypeArguments() = actualTypeArguments
        override fun getRawType() = Collection::class.java
        override fun getOwnerType() = null
    }
}
