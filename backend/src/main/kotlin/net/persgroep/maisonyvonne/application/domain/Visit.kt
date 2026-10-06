package net.persgroep.maisonyvonne.application.domain

import net.persgroep.maisonyvonne.application.command.RegisterVisitCommand
import net.persgroep.maisonyvonne.application.command.UpdateVisitCommand
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.*

@Document
data class Visit(
    val id: String = UUID.randomUUID().toString(),
    val seniorId: String,
    val scheduledAt: Instant,
    val completed: Boolean? = false,
    val yvonneCheck: YvonneCheck? = null,
    val meals: List<Meal>? = emptyList(),
    val randomInfo: String? = null
) {
    companion object {
        fun create(registerVisitCommand: RegisterVisitCommand): Visit = Visit(
            id = UUID.randomUUID().toString(),
            seniorId = registerVisitCommand.seniorId,
            scheduledAt = registerVisitCommand.date,
            meals = registerVisitCommand.meals?.toList(),
            randomInfo = registerVisitCommand.randomInfo
        )
    }

    fun update(updateVisitCommand: UpdateVisitCommand) = this.copy(
        scheduledAt = updateVisitCommand.date,
        meals = updateVisitCommand.meals?.toList(),
        randomInfo = updateVisitCommand.randomInfo
    )
}