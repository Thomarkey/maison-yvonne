package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.domain.UserRole
import net.persgroep.maisonyvonne.infrastructure.config.security.MaisonYvonnePrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class SessionDTO(val displayName: String, val role: UserRole)

@RestController
@RequestMapping("/api")
class SessionController {

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: MaisonYvonnePrincipal) =
        SessionDTO(displayName = principal.displayName, role = principal.role)
}
