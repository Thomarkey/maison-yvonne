package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.Visit

interface VisitRepository {
    fun add(visit: Visit)

    /** Dish names already used, newest first — feeds the admin form's autocomplete. */
    fun recentDishNames(limit: Int): List<String>
}
