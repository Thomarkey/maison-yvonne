package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.AccountDocument
import net.persgroep.maisonyvonne.application.domain.Account
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.stereotype.Repository

@Repository
class MongoAccountRepository(private val mongo: MongoTemplate) : AccountRepository {

    /** E-mails are stored lowercase, so the lookup is case-insensitive and still indexed. */
    override fun findByEmail(email: String): Account? =
        mongo.findOne(query(where("email").`is`(email.lowercase())), AccountDocument::class.java)
            ?.toDomain()
}
