package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.VisitDocument
import net.persgroep.maisonyvonne.application.domain.Visit
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

@Repository
class MongoVisitRepository(private val mongo: MongoTemplate) : VisitRepository {

    /** A visit and its three meals are one document, so there is nothing to keep in sync. */
    override fun add(visit: Visit) {
        mongo.insert(VisitDocument.from(visit))
    }

    override fun recentDishNames(limit: Int): List<String> =
        mongo.find(
            Query().with(Sort.by(Sort.Direction.DESC, "scheduledAt")).limit(limit),
            VisitDocument::class.java,
        )
            .flatMap { visit -> visit.meals.map { it.dishName } }
            .distinct()
}
