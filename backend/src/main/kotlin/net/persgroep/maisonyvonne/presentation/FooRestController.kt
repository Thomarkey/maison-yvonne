package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.service.FooService
import net.persgroep.maisonyvonne.presentation.api.CreateFooDTO
import net.persgroep.maisonyvonne.presentation.api.FooDTO
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/foos")
class FooRestController(
    private val fooService: FooService,
) {

    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: MaisonYvonnePrincipal,
        @RequestBody createFooDTO: CreateFooDTO,
    ): FooDTO =
        fooService.create(createFooDTO.toCreateFooCommand(userId = principal.userId))
            .let { FooDTO.from(it) }

    // GET takes no body and no redundant userId: it is already on the principal
    @GetMapping("/{fooId}")
    fun retrieve(
        @AuthenticationPrincipal principal: MaisonYvonnePrincipal,
        @PathVariable fooId: String,
    ): FooDTO =
        FooDTO.from(fooService.retrieve(fooId = fooId))
}
