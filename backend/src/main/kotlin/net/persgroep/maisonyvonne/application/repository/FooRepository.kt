package net.persgroep.maisonyvonne.application.repository

import net.persgroep.maisonyvonne.application.domain.foo.Foo
import net.persgroep.maisonyvonne.application.domain.foo.FooStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface FooRepository : MongoRepository<Foo, String> {

    fun findByUserIdAndStatus(userId: String, status: FooStatus): List<Foo>

    fun findByName(name: String): Foo?
}
