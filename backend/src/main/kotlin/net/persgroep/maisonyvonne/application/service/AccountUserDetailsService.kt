package net.persgroep.maisonyvonne.application.service

import net.persgroep.maisonyvonne.application.repository.AccountRepository
import net.persgroep.maisonyvonne.infrastructure.config.security.MaisonYvonnePrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AccountUserDetailsService(private val accounts: AccountRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails =
        accounts.findByEmail(username)
            ?.let { MaisonYvonnePrincipal(it) }
            ?: throw UsernameNotFoundException("unknown account") // no e-mail in logs
}
