package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.Account
import java.util.UUID

interface AccountRepository {
    fun find(id: UUID): Account?

    fun findAll(): List<Account>

    fun findByEmail(email: String): Account?

    /** Display names by account id; unknown ids are simply missing from the map. */
    fun displayNames(ids: Set<UUID>): Map<UUID, String>

    fun create(account: Account)

    fun updatePasswordHash(id: UUID, passwordHash: String)
}
