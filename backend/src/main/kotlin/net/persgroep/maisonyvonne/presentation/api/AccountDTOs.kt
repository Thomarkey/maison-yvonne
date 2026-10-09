package net.persgroep.maisonyvonne.presentation.api

import net.persgroep.maisonyvonne.application.command.ChangePasswordCommand
import net.persgroep.maisonyvonne.application.command.CreateAccountCommand
import net.persgroep.maisonyvonne.application.command.ResetPasswordCommand
import net.persgroep.maisonyvonne.application.domain.Account
import net.persgroep.maisonyvonne.application.domain.FamilyLink
import net.persgroep.maisonyvonne.application.domain.UserRole
import java.util.UUID

data class AccountDTO(
    val id: UUID,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val links: List<FamilyLinkDTO>,
) {
    companion object {
        fun from(account: Account) = AccountDTO(
            id = account.id,
            email = account.email,
            displayName = account.displayName,
            role = account.role,
            links = account.links.map { FamilyLinkDTO.from(it) },
        )
    }
}

data class FamilyLinkDTO(val seniorId: UUID, val relationLabel: String?) {
    companion object {
        fun from(link: FamilyLink) = FamilyLinkDTO(seniorId = link.seniorId, relationLabel = link.relationLabel)
    }
}

data class CreateAccountRequest(
    val email: String,
    val displayName: String,
    val role: UserRole,
    val password: String,
    /** Family accounts only: the senior they may follow. */
    val seniorId: UUID?,
    val relationLabel: String?,
) {
    fun toCreateAccountCommand() = CreateAccountCommand(
        email = email.trim(),
        displayName = displayName.trim(),
        role = role,
        password = password,
        link = seniorId?.let { FamilyLink(it, relationLabel?.trim()?.ifBlank { null }) },
    )
}

data class ResetPasswordRequest(val newPassword: String) {
    fun toResetPasswordCommand(accountId: UUID) = ResetPasswordCommand(accountId = accountId, newPassword = newPassword)
}

data class ChangePasswordRequest(val currentPassword: String, val newPassword: String) {
    fun toChangePasswordCommand(accountId: UUID) =
        ChangePasswordCommand(accountId = accountId, currentPassword = currentPassword, newPassword = newPassword)
}
