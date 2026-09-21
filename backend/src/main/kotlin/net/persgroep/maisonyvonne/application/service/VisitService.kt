package net.persgroep.maisonyvonne.application.service

import net.persgroep.maisonyvonne.application.command.RegisterVisitCommand
import net.persgroep.maisonyvonne.application.domain.MealSlot
import net.persgroep.maisonyvonne.application.domain.Visit
import net.persgroep.maisonyvonne.application.domain.YvonneCheck
import net.persgroep.maisonyvonne.application.repository.SeniorRepository
import net.persgroep.maisonyvonne.application.repository.VisitRepository
import org.springframework.stereotype.Service
import java.time.Clock
import java.util.UUID

@Service
class VisitService(
    private val visits: VisitRepository,
    private val seniors: SeniorRepository,
    private val clock: Clock,
) {

    /**
     * The admin form posts once and the visit goes live immediately: for the pilot
     * there is no draft step. A visit only becomes visible to family once published.
     */
    fun registerAndPublish(command: RegisterVisitCommand): Visit {
        requireNotNull(seniors.find(command.seniorId)) { "unknown senior ${command.seniorId}" }
        require(command.check != YvonneCheck.ATTENTION || !command.internalNote.isNullOrBlank()) {
            "an attention point needs a short internal note"
        }
        require(!command.completed || command.meals.map { it.slot }.toSet() == MealSlot.entries.toSet()) {
            "a completed visit carries all three meals"
        }

        return Visit(
            id = UUID.randomUUID(),
            seniorId = command.seniorId,
            scheduledAt = command.scheduledAt,
            completed = command.completed,
            check = command.check,
            familyMessage = command.familyMessage,
            momentOfWeek = command.momentOfWeek?.takeIf { it.isNotBlank() },
            internalNote = command.internalNote?.takeIf { it.isNotBlank() },
            meals = command.meals,
            nextVisitAt = command.nextVisitAt,
            createdBy = command.registeredBy,
        ).publish(clock.instant()).also { visits.add(it) }
    }

    fun seniors() = seniors.findAll()

    fun recentDishNames() = visits.recentDishNames(limit = 50)
}
