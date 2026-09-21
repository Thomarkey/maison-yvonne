package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.service.FamilyService
import net.persgroep.maisonyvonne.presentation.api.FamilyDashboardDTO
import net.persgroep.maisonyvonne.infrastructure.config.security.MaisonYvonnePrincipal
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/family")
class FamilyController(private val family: FamilyService) {

    @GetMapping("/dashboard")
    fun dashboard(@AuthenticationPrincipal principal: MaisonYvonnePrincipal) =
        family.dashboardFor(principal.userId).map { FamilyDashboardDTO.from(it) }
}
