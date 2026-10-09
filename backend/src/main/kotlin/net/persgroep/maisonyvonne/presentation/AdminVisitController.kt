package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.service.VisitService
import net.persgroep.maisonyvonne.infrastructure.config.security.MaisonYvonnePrincipal
import net.persgroep.maisonyvonne.presentation.api.AdminVisitDTO
import net.persgroep.maisonyvonne.presentation.api.PublishedVisitDTO
import net.persgroep.maisonyvonne.presentation.api.VisitRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin")
class AdminVisitController(private val visits: VisitService) {

    @GetMapping("/dish-names")
    fun dishNames() = visits.recentDishNames()

    @GetMapping("/visits")
    fun visits() = visits.visits().map { AdminVisitDTO.from(it) }

    @GetMapping("/visits/mine")
    fun myVisits(@AuthenticationPrincipal principal: MaisonYvonnePrincipal) =
        visits.visitsBy(principal.userId).map { AdminVisitDTO.from(it) }

    @PostMapping("/visits")
    fun register(
        @RequestBody request: VisitRequest,
        @AuthenticationPrincipal principal: MaisonYvonnePrincipal,
    ) = PublishedVisitDTO.from(visits.registerAndPublish(request.toRegisterVisitCommand(principal.userId)))

    @PutMapping("/visits/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: VisitRequest,
        @AuthenticationPrincipal principal: MaisonYvonnePrincipal,
    ) = AdminVisitDTO.from(visits.update(request.toUpdateVisitCommand(id, principal.userId)), principal.displayName)
}
