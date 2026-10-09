package net.persgroep.maisonyvonne.presentation.api

import net.persgroep.maisonyvonne.application.command.CreateFooCommand
import net.persgroep.maisonyvonne.application.domain.foo.Foo

data class FooDTO(
    val id: String,
    val name: String,
    val amount: Long,
    val status: String,
) {
    companion object {
        fun from(foo: Foo) = FooDTO(
            id = foo.id,
            name = foo.name,
            amount = foo.amount,
            status = foo.status.name,
        )
    }
}

data class CreateFooDTO(
    val name: String,
    val amount: Long,
) {
    // body fields + principal fields are stitched together here, not in the service
    fun toCreateFooCommand(userId: String) = CreateFooCommand(
        name = name,
        amount = amount,
        userId = userId,
    )
}
