package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.Visit
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface VisitRepository : MongoRepository<Visit, String> {

    fun getById(id: String): Visit?

}