package net.persgroep.maisonyvonne.application.command

data class CreateFooCommand(
    val name: String,
    val amount: Long,
    val userId: String,
)
