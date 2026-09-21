package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.SeniorUpdate
import java.util.UUID

interface FamilyDashboardRepository {
    /**
     * The seniors [userId] is linked to, each with their latest published visit.
     * The link is part of the query: there is no way to ask for a senior you are
     * not linked to, and unpublished visits are never returned.
     */
    fun dashboardFor(userId: UUID): List<SeniorUpdate>
}
