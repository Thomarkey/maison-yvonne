package net.persgroep.maisonyvonne.infrastructure.adapter

import net.persgroep.maisonyvonne.application.command.CreateAccountCommand
import net.persgroep.maisonyvonne.application.domain.UserRole
import net.persgroep.maisonyvonne.application.repository.AccountRepository
import net.persgroep.maisonyvonne.application.service.AccountService
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * First login on an empty database: creates the admin from `ADMIN_EMAIL` / `ADMIN_PASSWORD`
 * (and optional `ADMIN_NAME`) if no account with that e-mail exists yet. Never overwrites it,
 * so a password changed in the app stays changed.
 */
@Component
@ConditionalOnProperty(name = ["admin.email", "admin.password"])
class AdminBootstrap(
    private val accounts: AccountRepository,
    private val accountService: AccountService,
    @Value("\${admin.email}") private val email: String,
    @Value("\${admin.password}") private val password: String,
    @Value("\${admin.name:Admin}") private val name: String,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        accounts.findByEmail(email) ?: accountService.create(
            CreateAccountCommand(email = email, displayName = name, role = UserRole.ADMIN, password = password, link = null),
        )
    }
}
