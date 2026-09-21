package net.persgroep.maisonyvonne.application.domain

import java.util.UUID

data class Account(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val displayName: String,
)
