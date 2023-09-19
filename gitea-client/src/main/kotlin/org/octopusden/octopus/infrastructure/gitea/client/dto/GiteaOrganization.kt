package org.octopusden.octopus.infrastructure.gitea.client.dto

data class GiteaOrganization(val id: String, val name: String, val fullName: String?) : BaseGiteaEntity()
