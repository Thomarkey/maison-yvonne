package net.persgroep.maisonyvonne.application.domain

import java.util.UUID


data class Senior(
    val id: UUID,
    val preferredName: String,
    val fullName: String,
)
