package net.persgroep.maisonyvonne.application.service

import net.persgroep.maisonyvonne.application.domain.SeniorUpdate
import net.persgroep.maisonyvonne.application.repository.FamilyDashboardRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FamilyService(private val dashboards: FamilyDashboardRepository) {

    /**
     * [userId] comes from the authenticated principal, never from the request, so a
     * family member can only ever read the seniors they are linked to.
     */
    fun dashboardFor(userId: UUID): List<SeniorUpdate> = dashboards.dashboardFor(userId)
}
