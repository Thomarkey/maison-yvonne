package net.persgroep.maisonyvonne.infrastructure.config.security

import net.persgroep.maisonyvonne.application.domain.Account
import net.persgroep.maisonyvonne.application.domain.UserRole
import org.springframework.security.core.CredentialsContainer
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

/**
 * The authenticated caller. Controllers read [userId] from here and never accept it as a
 * path variable, request param or body field.
 *
 * Stored in the Mongo session (JDK-serialized), so it copies only the fields it needs from
 * [Account] and drops the password hash once login has checked it.
 */
class MaisonYvonnePrincipal(account: Account) : UserDetails, CredentialsContainer {

    val userId = account.id
    val displayName = account.displayName
    val role: UserRole = account.role
    private val email = account.email
    private var passwordHash: String? = account.passwordHash

    override fun getUsername() = email

    override fun getPassword() = passwordHash

    override fun getAuthorities() = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun eraseCredentials() {
        passwordHash = null
    }
}
