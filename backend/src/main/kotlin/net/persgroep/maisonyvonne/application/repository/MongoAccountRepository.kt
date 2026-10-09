package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.AccountDocument
import net.persgroep.maisonyvonne.application.domain.Account
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query.query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class MongoAccountRepository(private val mongo: MongoTemplate) : AccountRepository {

    override fun find(id: UUID): Account? =
        mongo.findById(id.toString(), AccountDocument::class.java)?.toDomain()

    override fun findAll(): List<Account> =
        mongo.findAll(AccountDocument::class.java).map { it.toDomain() }.sortedBy { it.displayName.lowercase() }

    /** E-mails are stored lowercase, so the lookup is case-insensitive and still indexed. */
    override fun findByEmail(email: String): Account? =
        mongo.findOne(query(where("email").`is`(email.lowercase())), AccountDocument::class.java)
            ?.toDomain()

    override fun displayNames(ids: Set<UUID>): Map<UUID, String> =
        mongo.find(query(where("_id").`in`(ids.map { it.toString() })), AccountDocument::class.java)
            .associate { UUID.fromString(it.id) to it.displayName }

    override fun create(account: Account) {
        mongo.insert(AccountDocument.from(account))
    }

    /** Only touches the hash, so the embedded family links stay as they are. */
    override fun updatePasswordHash(id: UUID, passwordHash: String) {
        mongo.updateFirst(
            query(where("_id").`is`(id.toString())),
            Update.update("passwordHash", passwordHash),
            AccountDocument::class.java,
        )
    }
}
