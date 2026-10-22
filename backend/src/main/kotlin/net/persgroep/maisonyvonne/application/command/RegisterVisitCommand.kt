package net.persgroep.maisonyvonne.application.command

import net.persgroep.maisonyvonne.application.domain.Meal
import net.persgroep.maisonyvonne.application.domain.Visit
import net.persgroep.maisonyvonne.application.domain.YvonneCheck
import java.time.Instant
import java.util.UUID

/** What an admin fills in on the visit form, whether registering or editing. */
data class VisitDetails(
    val seniorId: UUID,
    val scheduledAt: Instant,
    val completed: Boolean,
    val check: YvonneCheck,
    val familyMessage: String,
    val momentOfWeek: String?,
    val internalNote: String?,
    val meals: List<Meal>,
    val nextVisitAt: Instant?,
    val picture: String? = null,
) {
    fun toVisit(id: UUID, createdBy: UUID, publishedAt: Instant?) = Visit(
        id = id,
        seniorId = seniorId,
        scheduledAt = scheduledAt,
        completed = completed,
        check = check,
        familyMessage = familyMessage,
        momentOfWeek = momentOfWeek?.takeIf { it.isNotBlank() },
        internalNote = internalNote?.takeIf { it.isNotBlank() },
        meals = meals,
        nextVisitAt = nextVisitAt,
        createdBy = createdBy,
        publishedAt = publishedAt,
        picture = picture,
    )
}

data class RegisterVisitCommand(
    val details: VisitDetails,
    val registeredBy: UUID,
)

data class UpdateVisitCommand(
    val visitId: UUID,
    val details: VisitDetails,
    val editedBy: UUID,
)
