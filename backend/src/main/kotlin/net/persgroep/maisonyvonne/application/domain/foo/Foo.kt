package net.persgroep.maisonyvonne.application.domain.foo

import net.persgroep.maisonyvonne.application.domain.AbstractBase
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document
data class Foo(
    @Id
    val id: String,
    val name: String,
    val amount: Long,
    val status: FooStatus = FooStatus.CREATED,
    val createdOn: Instant = Instant.now(),
) : AbstractBase() {

    // small, self-contained operation on own state: returns a new Foo, triggers nothing else
    fun activate(): Foo = copy(status = FooStatus.ACTIVE)

    fun isActive(): Boolean = status == FooStatus.ACTIVE
}
