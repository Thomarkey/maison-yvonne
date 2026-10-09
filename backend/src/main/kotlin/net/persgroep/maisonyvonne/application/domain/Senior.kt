package net.persgroep.maisonyvonne.application.domain

import java.util.UUID

data class Senior(
    val id: UUID,
    val preferredName: String,
    val fullName: String,
) {
    init {
        require(preferredName.isNotBlank()) { "a senior needs a name to be called by" }
        require(fullName.isNotBlank()) { "a senior needs a full name" }
    }
}
