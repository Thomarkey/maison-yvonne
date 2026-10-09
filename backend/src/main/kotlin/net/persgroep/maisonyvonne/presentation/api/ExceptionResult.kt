package net.persgroep.maisonyvonne.presentation.api

class ExceptionResult(
    val status: Int,
    val message: String?,
    val type: String? = null,
)
