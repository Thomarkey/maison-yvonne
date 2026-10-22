package net.persgroep.maisonyvonne.presentation.api

import net.persgroep.maisonyvonne.application.command.CreateSeniorCommand
import net.persgroep.maisonyvonne.application.command.RegisterVisitCommand
import net.persgroep.maisonyvonne.application.command.UpdateSeniorCommand
import net.persgroep.maisonyvonne.application.command.UpdateVisitCommand
import net.persgroep.maisonyvonne.application.command.VisitDetails
import net.persgroep.maisonyvonne.application.domain.Meal
import net.persgroep.maisonyvonne.application.domain.MealSlot
import net.persgroep.maisonyvonne.application.domain.RegisteredVisit
import net.persgroep.maisonyvonne.application.domain.Senior
import net.persgroep.maisonyvonne.application.domain.SeniorUpdate
import net.persgroep.maisonyvonne.application.domain.Visit
import net.persgroep.maisonyvonne.application.domain.YvonneCheck
import java.time.Instant
import java.util.UUID

data class SeniorDTO(val id: UUID, val preferredName: String, val fullName: String) {
    companion object {
        fun from(senior: Senior) = SeniorDTO(
            id = senior.id,
            preferredName = senior.preferredName,
            fullName = senior.fullName,
        )
    }
}

data class SeniorRequest(val preferredName: String?, val fullName: String) {
    fun toCreateSeniorCommand() =
        CreateSeniorCommand(preferredName = preferredName?.trim(), fullName = fullName.trim())

    fun toUpdateSeniorCommand(seniorId: UUID) =
        UpdateSeniorCommand(seniorId = seniorId, preferredName = preferredName?.trim(), fullName = fullName.trim())
}

data class MealRequest(val slot: MealSlot, val dishName: String, val storageInfo: String) {
    fun toMeal() = Meal(slot = slot, dishName = dishName.trim(), storageInfo = storageInfo.trim())
}

/** The visit form's body, for registering a visit as well as for editing one. */
data class VisitRequest(
    val seniorId: UUID,
    val scheduledAt: Instant,
    val completed: Boolean,
    val check: YvonneCheck,
    val familyMessage: String,
    val momentOfWeek: String?,
    val internalNote: String?,
    val meals: List<MealRequest>,
    val nextVisitAt: Instant?,
    val picture: String? = null,
) {
    /** [registeredBy] comes from the principal, never from the request body. */
    fun toRegisterVisitCommand(registeredBy: UUID) =
        RegisterVisitCommand(details = toVisitDetails(), registeredBy = registeredBy)

    /** [editedBy] comes from the principal, never from the request body. */
    fun toUpdateVisitCommand(visitId: UUID, editedBy: UUID) =
        UpdateVisitCommand(visitId = visitId, details = toVisitDetails(), editedBy = editedBy)

    private fun toVisitDetails() = VisitDetails(
        seniorId = seniorId,
        scheduledAt = scheduledAt,
        completed = completed,
        check = check,
        familyMessage = familyMessage.trim(),
        momentOfWeek = momentOfWeek?.trim(),
        internalNote = internalNote?.trim(),
        meals = meals.map { it.toMeal() },
        nextVisitAt = nextVisitAt,
        picture = picture,
    )
}

data class PublishedVisitDTO(val id: UUID, val publishedAt: Instant?) {
    companion object {
        fun from(visit: Visit) = PublishedVisitDTO(id = visit.id, publishedAt = visit.publishedAt)
    }
}

data class MealDTO(val slot: MealSlot, val label: String, val dishName: String, val storageInfo: String) {
    companion object {
        fun from(meal: Meal) = MealDTO(
            slot = meal.slot,
            label = meal.slot.label,
            dishName = meal.dishName,
            storageInfo = meal.storageInfo,
        )
    }
}

/** What admins see in their visit overview, internal note included. */
data class AdminVisitDTO(
    val id: UUID,
    val seniorId: UUID,
    val scheduledAt: Instant,
    val completed: Boolean,
    val check: YvonneCheck,
    val familyMessage: String,
    val momentOfWeek: String?,
    val internalNote: String?,
    val meals: List<MealDTO>,
    val nextVisitAt: Instant?,
    val publishedAt: Instant?,
    val picture: String?,
    /** Display name of the admin who registered the visit; null if that account is gone. */
    val registeredBy: String?,
) {
    companion object {
        fun from(registered: RegisteredVisit) = from(registered.visit, registered.registeredBy)

        fun from(visit: Visit, registeredBy: String?) = AdminVisitDTO(
            id = visit.id,
            seniorId = visit.seniorId,
            scheduledAt = visit.scheduledAt,
            completed = visit.completed,
            check = visit.check,
            familyMessage = visit.familyMessage,
            momentOfWeek = visit.momentOfWeek,
            internalNote = visit.internalNote,
            meals = visit.meals.map { MealDTO.from(it) },
            nextVisitAt = visit.nextVisitAt,
            publishedAt = visit.publishedAt,
            picture = visit.picture,
            registeredBy = registeredBy,
        )
    }
}

/** What family receives. There is no field for the internal note — by design. */
data class FamilyVisitDTO(
    val visitedAt: Instant,
    val completed: Boolean,
    val check: YvonneCheck,
    val familyMessage: String,
    val momentOfWeek: String?,
    val meals: List<MealDTO>,
    val nextVisitAt: Instant?,
    val picture: String?,
) {
    companion object {
        fun from(visit: Visit) = FamilyVisitDTO(
            visitedAt = visit.scheduledAt,
            completed = visit.completed,
            check = visit.check,
            familyMessage = visit.familyMessage,
            momentOfWeek = visit.momentOfWeek,
            meals = visit.meals.map { MealDTO.from(it) },
            nextVisitAt = visit.nextVisitAt,
            picture = visit.picture,
        )
    }
}

data class FamilyDashboardDTO(
    val senior: SeniorDTO,
    val relationLabel: String?,
    val lastVisit: FamilyVisitDTO?,
) {
    companion object {
        fun from(update: SeniorUpdate) = FamilyDashboardDTO(
            senior = SeniorDTO.from(update.senior),
            relationLabel = update.relationLabel,
            lastVisit = update.latestVisit?.let { FamilyVisitDTO.from(it) },
        )
    }
}
