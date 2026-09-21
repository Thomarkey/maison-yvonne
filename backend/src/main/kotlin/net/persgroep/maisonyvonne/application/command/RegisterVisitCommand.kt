package net.persgroep.maisonyvonne.application.command

import net.persgroep.maisonyvonne.application.domain.Meal
import net.persgroep.maisonyvonne.application.domain.YvonneCheck
import java.time.Instant
import java.util.UUID

data class RegisterVisitCommand(
    val seniorId: UUID,
    val scheduledAt: Instant,
    val completed: Boolean,
    val check: YvonneCheck,
    val familyMessage: String,
    val momentOfWeek: String?,
    val internalNote: String?,
    val meals: List<Meal>,
    val nextVisitAt: Instant?,
    val registeredBy: UUID,
)
