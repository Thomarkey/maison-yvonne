package net.persgroep.maisonyvonne.application.command

import net.persgroep.maisonyvonne.application.domain.Meal
import java.time.Instant

data class UpdateVisitCommand(
    val visitId: String,
    val date: Instant,
    val meals: Set<Meal>? = emptySet(),
    val randomInfo: String? = null
)
