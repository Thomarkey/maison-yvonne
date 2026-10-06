package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.Senior
import org.springframework.data.mongodb.repository.MongoRepository

interface SeniorRepository : MongoRepository<Senior, String> {

    fun getById(id: String): Senior?
    fun existsByName(name: String): Boolean

}