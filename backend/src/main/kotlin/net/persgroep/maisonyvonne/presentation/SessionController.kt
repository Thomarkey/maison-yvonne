package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.domain.UserRole
import net.persgroep.maisonyvonne.application.service.AccountService
import net.persgroep.maisonyvonne.infrastructure.config.security.MaisonYvonnePrincipal
import net.persgroep.maisonyvonne.presentation.api.ChangePasswordRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

data class SessionDTO(val displayName: String, val email: String, val role: UserRole)

@RestController
@RequestMapping("/api")
class SessionController(private val accounts: AccountService) {

    @GetMapping("/me")
    fun me(@AuthenticationPrincipal principal: MaisonYvonnePrincipal) =
        SessionDTO(displayName = principal.displayName, email = principal.username, role = principal.role)

    @PostMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun changePassword(
        @AuthenticationPrincipal principal: MaisonYvonnePrincipal,
        @RequestBody request: ChangePasswordRequest,
    ) = accounts.changePassword(request.toChangePasswordCommand(principal.userId))
}
