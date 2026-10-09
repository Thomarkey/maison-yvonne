package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.Visit
import java.util.UUID

interface VisitRepository {
    fun add(visit: Visit)

    fun replace(visit: Visit)

    fun find(id: UUID): Visit?

    /** Every visit, newest first. Admin only: it carries the internal note. */
    fun findAll(): List<Visit>

    /** The visits [userId] registered, newest first. */
    fun findByCreatedBy(userId: UUID): List<Visit>

    /** Dish names already used, newest first — feeds the admin form's autocomplete. */
    fun recentDishNames(limit: Int): List<String>
}
