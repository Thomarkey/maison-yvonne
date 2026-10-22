package net.persgroep.maisonyvonne.application.domain

import java.time.Instant
import java.util.UUID

enum class MealSlot(val slot: Int, val label: String) {
    FIRST(1, "Eerst van genieten"),
    SECOND(2, "Daarna"),
    LAST(3, "Bewaar mij voor het laatst"),
    ;

    companion object {
        fun from(slot: Int) = entries.first { it.slot == slot }
    }
}

enum class YvonneCheck {
    ALL_AS_USUAL,
    ATTENTION,
}

data class Meal(
    val slot: MealSlot,
    val dishName: String,
    val storageInfo: String,
)

data class Visit(
    val id: UUID,
    val seniorId: UUID,
    val scheduledAt: Instant,
    val completed: Boolean,
    val check: YvonneCheck,
    val familyMessage: String,
    val momentOfWeek: String?,
    /** Factual note for admin only — never exposed to family. */
    val internalNote: String?,
    val meals: List<Meal>,
    val nextVisitAt: Instant?,
    val createdBy: UUID,
    val publishedAt: Instant? = null,
    val picture: String? = null,
) {
    init {
        require(familyMessage.isNotBlank()) { "a visit needs an update for the family" }
    }

    fun publish(at: Instant) = copy(publishedAt = at)
}
