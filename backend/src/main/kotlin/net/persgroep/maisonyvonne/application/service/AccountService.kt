package net.persgroep.maisonyvonne.application.service

import net.persgroep.maisonyvonne.application.command.ChangePasswordCommand
import net.persgroep.maisonyvonne.application.command.CreateAccountCommand
import net.persgroep.maisonyvonne.application.command.ResetPasswordCommand
import net.persgroep.maisonyvonne.application.domain.Account
import net.persgroep.maisonyvonne.application.domain.UserRole
import net.persgroep.maisonyvonne.application.repository.AccountRepository
import net.persgroep.maisonyvonne.application.repository.SeniorRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AccountService(
    private val accounts: AccountRepository,
    private val seniors: SeniorRepository,
    private val passwords: PasswordEncoder,
) {

    fun accounts() = accounts.findAll()

    fun create(command: CreateAccountCommand): Account {
        require(command.email.isNotBlank() && command.displayName.isNotBlank()) { "E-mailadres en naam zijn verplicht." }
        require(accounts.findByEmail(command.email) == null) { "Er bestaat al een account met dit e-mailadres." }
        val link = command.link?.takeIf { command.role == UserRole.FAMILY }
        require(command.role != UserRole.FAMILY || link != null) { "Kies de senior die dit familielid mag volgen." }
        link?.let { seniors.find(it.seniorId) ?: throw NoSuchElementException("unknown senior ${it.seniorId}") }

        return Account(
            id = UUID.randomUUID(),
            email = command.email.lowercase(),
            passwordHash = hash(command.password),
            role = command.role,
            displayName = command.displayName,
            links = listOfNotNull(link),
        ).also { accounts.create(it) }
    }

    fun changePassword(command: ChangePasswordCommand) {
        val account = accounts.find(command.accountId) ?: throw NoSuchElementException("unknown account")
        require(passwords.matches(command.currentPassword, account.passwordHash)) { "Je huidige wachtwoord klopt niet." }
        accounts.updatePasswordHash(account.id, hash(command.newPassword))
    }

    fun resetPassword(command: ResetPasswordCommand) {
        val account = accounts.find(command.accountId) ?: throw NoSuchElementException("unknown account ${command.accountId}")
        accounts.updatePasswordHash(account.id, hash(command.newPassword))
    }

    private fun hash(password: String): String {
        require(password.length >= MIN_PASSWORD_LENGTH) { "Een wachtwoord moet minstens $MIN_PASSWORD_LENGTH tekens hebben." }
        return checkNotNull(passwords.encode(password)) { "encoder returned no hash" }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
