package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.service.VisitService
import net.persgroep.maisonyvonne.presentation.api.PublishedVisitDTO
import net.persgroep.maisonyvonne.presentation.api.RegisterVisitRequest
import net.persgroep.maisonyvonne.presentation.api.SeniorDTO
import net.persgroep.maisonyvonne.infrastructure.config.security.MaisonYvonnePrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
class AdminVisitController(private val visits: VisitService) {

    @GetMapping("/seniors")
    fun seniors() = visits.seniors().map { SeniorDTO.from(it) }

    @GetMapping("/dish-names")
    fun dishNames() = visits.recentDishNames()

    @PostMapping("/visits")
    fun register(
        @RequestBody request: RegisterVisitRequest,
        @AuthenticationPrincipal principal: MaisonYvonnePrincipal,
    ) = PublishedVisitDTO.from(visits.registerAndPublish(request.toRegisterVisitCommand(principal.userId)))
}
