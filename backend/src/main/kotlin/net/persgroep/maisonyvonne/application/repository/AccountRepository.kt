package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.Account

interface AccountRepository {
    fun findByEmail(email: String): Account?
}
