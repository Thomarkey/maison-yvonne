package net.persgroep.maisonyvonne.presentation.api

import net.persgroep.maisonyvonne.application.command.RegisterVisitCommand
import net.persgroep.maisonyvonne.application.domain.Meal
import net.persgroep.maisonyvonne.application.domain.MealSlot
import net.persgroep.maisonyvonne.application.domain.Senior
import net.persgroep.maisonyvonne.application.domain.SeniorUpdate
import net.persgroep.maisonyvonne.application.domain.Visit
import net.persgroep.maisonyvonne.application.domain.YvonneCheck
import java.time.Instant
import java.util.UUID

data class SeniorDTO(val id: UUID, val preferredName: String) {
    companion object {
        fun from(senior: Senior) = SeniorDTO(id = senior.id, preferredName = senior.preferredName)
    }
}

data class MealRequest(val slot: MealSlot, val dishName: String, val storageInfo: String) {
    fun toMeal() = Meal(slot = slot, dishName = dishName.trim(), storageInfo = storageInfo.trim())
}

data class RegisterVisitRequest(
    val seniorId: UUID,
    val scheduledAt: Instant,
    val completed: Boolean,
    val check: YvonneCheck,
    val familyMessage: String,
    val momentOfWeek: String?,
    val internalNote: String?,
    val meals: List<MealRequest>,
    val nextVisitAt: Instant?,
) {
    /** [registeredBy] comes from the principal, never from the request body. */
    fun toRegisterVisitCommand(registeredBy: UUID) = RegisterVisitCommand(
        seniorId = seniorId,
        scheduledAt = scheduledAt,
        completed = completed,
        check = check,
        familyMessage = familyMessage.trim(),
        momentOfWeek = momentOfWeek?.trim(),
        internalNote = internalNote?.trim(),
        meals = meals.map { it.toMeal() },
        nextVisitAt = nextVisitAt,
        registeredBy = registeredBy,
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

/** What family receives. There is no field for the internal note — by design. */
data class FamilyVisitDTO(
    val visitedAt: Instant,
    val completed: Boolean,
    val check: YvonneCheck,
    val familyMessage: String,
    val momentOfWeek: String?,
    val meals: List<MealDTO>,
    val nextVisitAt: Instant?,
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
