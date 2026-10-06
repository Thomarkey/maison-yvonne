package net.persgroep.maisonyvonne.presentation.api

import net.persgroep.maisonyvonne.application.domain.Visit
import net.persgroep.maisonyvonne.application.domain.YvonneCheck
import java.time.Instant

data class VisitDTO(
    val id: String,
    val seniorId: String,
    val scheduledAt: Instant,
    val completed: Boolean? = false,
    val yvonneCheck: YvonneCheck? = null,
    val meals: List<MealDTO>? = emptyList(),
    val randomInfo: String? = null
) {
    companion object {
        fun from(visit: Visit) = VisitDTO(
            id = visit.id,
            seniorId = visit.seniorId,
            scheduledAt = visit.scheduledAt,
            completed = visit.completed,
            yvonneCheck = visit.yvonneCheck,
            meals = visit.meals?.map { MealDTO.from(it) },
            randomInfo = visit.randomInfo
        )
    }
}