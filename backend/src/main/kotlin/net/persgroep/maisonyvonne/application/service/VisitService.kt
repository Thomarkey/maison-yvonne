package net.persgroep.maisonyvonne.application.service

import net.persgroep.maisonyvonne.application.command.RegisterVisitCommand
import net.persgroep.maisonyvonne.application.command.UpdateVisitCommand
import net.persgroep.maisonyvonne.application.command.VisitDetails
import net.persgroep.maisonyvonne.application.domain.MealSlot
import net.persgroep.maisonyvonne.application.domain.NotYourVisitException
import net.persgroep.maisonyvonne.application.domain.RegisteredVisit
import net.persgroep.maisonyvonne.application.domain.Visit
import net.persgroep.maisonyvonne.application.domain.YvonneCheck
import net.persgroep.maisonyvonne.application.repository.AccountRepository
import net.persgroep.maisonyvonne.application.repository.SeniorRepository
import net.persgroep.maisonyvonne.application.repository.VisitRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.Base64
import java.util.UUID

@Service
class VisitService(
    private val visits: VisitRepository,
    private val seniors: SeniorRepository,
    private val accounts: AccountRepository,
    private val clock: Clock,
) {

    /**
     * The admin form posts once and the visit goes live immediately: for the pilot
     * there is no draft step. A visit only becomes visible to family once published.
     */
    fun registerAndPublish(command: RegisterVisitCommand): Visit {
        return validated(command.details)
            .toVisit(id = UUID.randomUUID(), createdBy = command.registeredBy, publishedAt = null)
            .publish(clock.instant())
            .also { visits.add(it) }
    }

    /** Only whoever registered a visit may correct it; who that was and when it went live stay as they were. */
    fun update(command: UpdateVisitCommand): Visit {
        val existing = visits.find(command.visitId) ?: throw NoSuchElementException("unknown visit ${command.visitId}")
        if (existing.createdBy != command.editedBy) throw NotYourVisitException(command.visitId)
        return validated(command.details)
            .toVisit(id = existing.id, createdBy = existing.createdBy, publishedAt = existing.publishedAt)
            .also { visits.replace(it) }
    }

    private fun validated(details: VisitDetails): VisitDetails {
        requireNotNull(seniors.find(details.seniorId)) { "unknown senior ${details.seniorId}" }
        validatePicture(details.picture)
        require(details.check != YvonneCheck.ATTENTION || !details.internalNote.isNullOrBlank()) {
            "an attention point needs a short internal note"
        }
        require(!details.completed || details.meals.map { it.slot }.toSet() == MealSlot.entries.toSet()) {
            "a completed visit carries all three meals"
        }
        return details
    }

    private fun validatePicture(picture: String?) {
        if (picture == null) return

        val match = PICTURE_PATTERN.matchEntire(picture)
            ?: throw IllegalArgumentException("picture must be a JPEG, PNG, or WebP image")
        require(match.groupValues[2].length <= MAX_ENCODED_PICTURE_LENGTH) { "picture must be no larger than 5 MB" }
        val bytes = try {
            Base64.getDecoder().decode(match.groupValues[2])
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("picture is not valid base64", e)
        }
        require(bytes.size <= MAX_PICTURE_BYTES) { "picture must be no larger than 5 MB" }

        val validSignature = when (match.groupValues[1]) {
            "jpeg" -> bytes.size >= 3 && bytes[0] == 0xff.toByte() && bytes[1] == 0xd8.toByte() && bytes[2] == 0xff.toByte()
            "png" -> bytes.size >= 8 && bytes.take(8) == PNG_SIGNATURE
            "webp" -> bytes.size >= 12 &&
                bytes.copyOfRange(0, 4).contentEquals("RIFF".toByteArray()) &&
                bytes.copyOfRange(8, 12).contentEquals("WEBP".toByteArray())
            else -> false
        }
        require(validSignature) { "picture content does not match its image type" }
    }

    private companion object {
        const val MAX_PICTURE_BYTES = 5 * 1024 * 1024
        const val MAX_ENCODED_PICTURE_LENGTH = ((MAX_PICTURE_BYTES + 2) / 3) * 4
        val PICTURE_PATTERN = Regex("""data:image/(jpeg|png|webp);base64,([A-Za-z0-9+/]*={0,2})""")
        val PNG_SIGNATURE = listOf(0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a).map { it.toByte() }
    }

    fun visits() = withRegisteredBy(visits.findAll())

    fun visitsBy(userId: UUID) = withRegisteredBy(visits.findByCreatedBy(userId))

    /** One account lookup for the whole list, not one per visit. */
    private fun withRegisteredBy(list: List<Visit>): List<RegisteredVisit> {
        val names = accounts.displayNames(list.map { it.createdBy }.toSet())
        return list.map { RegisteredVisit(visit = it, registeredBy = names[it.createdBy]) }
    }

    fun recentDishNames() = visits.recentDishNames(limit = 50)
}
