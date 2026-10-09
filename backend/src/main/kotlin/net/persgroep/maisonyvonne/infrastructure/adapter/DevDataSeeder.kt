package net.persgroep.maisonyvonne.infrastructure.adapter

import net.persgroep.maisonyvonne.application.domain.Account
import net.persgroep.maisonyvonne.application.domain.AccountDocument
import net.persgroep.maisonyvonne.application.domain.FamilyLink
import net.persgroep.maisonyvonne.application.domain.Senior
import net.persgroep.maisonyvonne.application.domain.SeniorDocument
import net.persgroep.maisonyvonne.application.domain.UserRole
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Pilot data for local development: Yasmina, Louis and Louisa's daughter Sophie.
 * Runs under the `dev` profile only and does nothing once the accounts exist.
 */
@Component
@Profile("dev")
class DevDataSeeder(
    private val mongo: MongoTemplate,
    private val passwords: PasswordEncoder,
) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (mongo.count(Query(), AccountDocument::class.java) > 0) return

        val louisa = Senior(
            id = UUID.randomUUID(),
            preferredName = "Louisa",
            fullName = "Louisa Vermeulen",
        )
        mongo.insert(SeniorDocument.from(louisa))

        mongo.insert(AccountDocument.from(account("yasmina@maisonyvonne.be", "Yasmina", UserRole.ADMIN)))
        mongo.insert(AccountDocument.from(account("louis@maisonyvonne.be", "Louis", UserRole.ADMIN)))
        mongo.insert(
            AccountDocument.from(
                account("sophie@example.com", "Sophie", UserRole.FAMILY, listOf(FamilyLink(louisa.id, "dochter"))),
            ),
        )
    }

    private fun account(email: String, displayName: String, role: UserRole, links: List<FamilyLink> = emptyList()) = Account(
        id = UUID.randomUUID(),
        email = email,
        passwordHash = checkNotNull(passwords.encode(DEMO_PASSWORD)) { "encoder returned no hash" },
        role = role,
        displayName = displayName,
        links = links,
    )

    private companion object {
        const val DEMO_PASSWORD = "yvonne"
    }
}
