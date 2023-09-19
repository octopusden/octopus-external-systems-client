package org.octopusden.octopus.infrastructure.gitea.client.dto

data class GiteaEntityList<T : BaseGiteaEntity>(val hasMore: Boolean?, val values: Collection<T>)
