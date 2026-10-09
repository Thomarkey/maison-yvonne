package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.Account
import java.util.UUID

interface AccountRepository {
    fun findByEmail(email: String): Account?

    /** Display names by account id; unknown ids are simply missing from the map. */
    fun displayNames(ids: Set<UUID>): Map<UUID, String>
}
