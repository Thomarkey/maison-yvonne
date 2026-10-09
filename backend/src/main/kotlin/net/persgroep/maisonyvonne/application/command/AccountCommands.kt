package net.persgroep.maisonyvonne.application.command

import net.persgroep.maisonyvonne.application.domain.FamilyLink
import net.persgroep.maisonyvonne.application.domain.UserRole
import java.util.UUID

/** [link] is only kept for [UserRole.FAMILY]; admins see every senior. */
data class CreateAccountCommand(
    val email: String,
    val displayName: String,
    val role: UserRole,
    val password: String,
    val link: FamilyLink?,
)

/** An admin sets a new starting password for someone who forgot theirs. */
data class ResetPasswordCommand(val accountId: UUID, val newPassword: String)

data class ChangePasswordCommand(val accountId: UUID, val currentPassword: String, val newPassword: String)
