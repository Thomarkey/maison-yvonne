package net.persgroep.maisonyvonne.infrastructure.gateway.externalService.api

import net.persgroep.maisonyvonne.application.domain.foo.Foo
import net.persgroep.maisonyvonne.application.domain.foo.FooStatus

data class FooDTO(
    val id: String,
    val name: String,
    val amount: Long,
    val state: String?,
) {
    fun toDomain() = Foo(
        id = id,
        name = name,
        amount = amount,
        status = state?.let { FooStatus.valueOf(it) } ?: FooStatus.CREATED,
    )

    companion object {
        fun from(foo: Foo) = FooDTO(
            id = foo.id,
            name = foo.name,
            amount = foo.amount,
            state = foo.status.name,
        )
    }
}
