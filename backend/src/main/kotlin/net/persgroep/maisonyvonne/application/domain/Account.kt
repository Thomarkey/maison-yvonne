package net.persgroep.maisonyvonne.application.domain

import java.util.UUID

data class Account(
    val id: UUID,
    val email: String,
    val passwordHash: String,
    val role: UserRole,
    val displayName: String,
    /** Which seniors this account may read. Empty for admins, who see everyone. */
    val links: List<FamilyLink> = emptyList(),
)
