package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.Senior
import java.util.UUID

interface SeniorRepository {
    fun findAll(): List<Senior>

    fun find(id: UUID): Senior?

    /** Inserts or overwrites by id. */
    fun save(senior: Senior)
}
