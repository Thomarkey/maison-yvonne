package net.persgroep.maisonyvonne.application.domain

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document("accounts")
data class AccountDocument(
    @Id val id: String,
    @Indexed(unique = true) val email: String,
    val passwordHash: String,
    val role: String,
    val displayName: String,
    /** Which seniors this account may read. Empty for admins, who see everyone. */
    val links: List<FamilyLinkDocument> = emptyList(),
) {
    fun toDomain() = Account(
        id = UUID.fromString(id),
        email = email,
        passwordHash = passwordHash,
        role = UserRole.valueOf(role),
        displayName = displayName,
        links = links.map { it.toDomain() },
    )

    companion object {
        fun from(account: Account) = AccountDocument(
            id = account.id.toString(),
            email = account.email.lowercase(),
            passwordHash = account.passwordHash,
            role = account.role.name,
            displayName = account.displayName,
            links = account.links.map { FamilyLinkDocument.from(it) },
        )
    }
}

data class FamilyLinkDocument(val seniorId: String, val relationLabel: String?) {
    fun toDomain() = FamilyLink(UUID.fromString(seniorId), relationLabel)

    companion object {
        fun from(link: FamilyLink) = FamilyLinkDocument(link.seniorId.toString(), link.relationLabel)
    }
}

@Document("seniors")
data class SeniorDocument(
    @Id val id: String,
    val preferredName: String,
    val fullName: String,
) {
    fun toDomain() = Senior(
        id = UUID.fromString(id),
        preferredName = preferredName,
        fullName = fullName,
    )

    companion object {
        fun from(senior: Senior) = SeniorDocument(
            id = senior.id.toString(),
            preferredName = senior.preferredName,
            fullName = senior.fullName,
        )
    }
}

data class MealDocument(val slot: Int, val dishName: String, val storageInfo: String) {
    fun toDomain() = Meal(
        slot = MealSlot.from(slot),
        dishName = dishName,
        storageInfo = storageInfo,
    )

    companion object {
        fun from(meal: Meal) = MealDocument(
            slot = meal.slot.slot,
            dishName = meal.dishName,
            storageInfo = meal.storageInfo,
        )
    }
}

@Document("visits")
data class VisitDocument(
    @Id val id: String,
    @Indexed val seniorId: String,
    val scheduledAt: Instant,
    val completed: Boolean,
    val checkStatus: String,
    val familyMessage: String,
    val momentOfWeek: String?,
    /** Admin-only. [toFamilyVisit] is the read family payloads are built from. */
    val internalNote: String?,
    val meals: List<MealDocument>,
    val nextVisitAt: Instant?,
    val createdBy: String,
    val publishedAt: Instant?,
) {
    /** Drops the internal note on the way out, so it cannot reach a family DTO. */
    fun toFamilyVisit() = toDomain(internalNote = null)

    /** Admin read: keeps the internal note. */
    fun toDomain() = toDomain(internalNote = internalNote)

    private fun toDomain(internalNote: String?) = Visit(
        id = UUID.fromString(id),
        seniorId = UUID.fromString(seniorId),
        scheduledAt = scheduledAt,
        completed = completed,
        check = YvonneCheck.valueOf(checkStatus),
        familyMessage = familyMessage,
        momentOfWeek = momentOfWeek,
        internalNote = internalNote,
        meals = meals.sortedBy { it.slot }.map { it.toDomain() },
        nextVisitAt = nextVisitAt,
        createdBy = UUID.fromString(createdBy),
        publishedAt = publishedAt,
    )

    companion object {
        fun from(visit: Visit) = VisitDocument(
            id = visit.id.toString(),
            seniorId = visit.seniorId.toString(),
            scheduledAt = visit.scheduledAt,
            completed = visit.completed,
            checkStatus = visit.check.name,
            familyMessage = visit.familyMessage,
            momentOfWeek = visit.momentOfWeek,
            internalNote = visit.internalNote,
            meals = visit.meals.map { MealDocument.from(it) },
            nextVisitAt = visit.nextVisitAt,
            createdBy = visit.createdBy.toString(),
            publishedAt = visit.publishedAt,
        )
    }
}
