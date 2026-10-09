package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.SeniorDocument
import net.persgroep.maisonyvonne.application.domain.Senior
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class MongoSeniorRepository(private val mongo: MongoTemplate) : SeniorRepository {

    override fun findAll(): List<Senior> =
        mongo.find(Query().with(Sort.by("preferredName")), SeniorDocument::class.java)
            .map { it.toDomain() }

    override fun find(id: UUID): Senior? =
        mongo.findById(id.toString(), SeniorDocument::class.java)?.toDomain()

    override fun save(senior: Senior) {
        mongo.save(SeniorDocument.from(senior))
    }
}
