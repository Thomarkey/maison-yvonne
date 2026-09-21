package net.persgroep.maisonyvonne.infrastructure.config.security

import net.persgroep.maisonyvonne.application.domain.Account
import net.persgroep.maisonyvonne.application.domain.UserRole
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * The authenticated caller. Controllers read [userId] from here and never accept it as a
 * path variable, request param or body field.
 */
class MaisonYvonnePrincipal(private val account: Account) : UserDetails {

    val userId = account.id
    val displayName = account.displayName
    val role: UserRole = account.role

    override fun getUsername() = account.email

    override fun getPassword() = account.passwordHash

    override fun getAuthorities() = listOf(SimpleGrantedAuthority("ROLE_${account.role.name}"))
}
