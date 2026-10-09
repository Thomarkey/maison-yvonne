package net.persgroep.maisonyvonne.application.command

data class CreateSeniorCommand(
    val name: String,
    val nickname: String? = null,
)