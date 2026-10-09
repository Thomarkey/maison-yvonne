package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.service.AccountService
import net.persgroep.maisonyvonne.presentation.api.AccountDTO
import net.persgroep.maisonyvonne.presentation.api.CreateAccountRequest
import net.persgroep.maisonyvonne.presentation.api.ResetPasswordRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/accounts")
class AdminAccountController(private val accounts: AccountService) {

    @GetMapping
    fun accounts() = accounts.accounts().map { AccountDTO.from(it) }

    @PostMapping
    fun create(@RequestBody request: CreateAccountRequest) =
        AccountDTO.from(accounts.create(request.toCreateAccountCommand()))

    @PutMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun resetPassword(@PathVariable id: UUID, @RequestBody request: ResetPasswordRequest) =
        accounts.resetPassword(request.toResetPasswordCommand(id))
}
