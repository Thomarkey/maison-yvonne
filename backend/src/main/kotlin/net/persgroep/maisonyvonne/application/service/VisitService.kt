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
        require(details.check != YvonneCheck.ATTENTION || !details.internalNote.isNullOrBlank()) {
            "an attention point needs a short internal note"
        }
        require(!details.completed || details.meals.map { it.slot }.toSet() == MealSlot.entries.toSet()) {
            "a completed visit carries all three meals"
        }
        return details
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
