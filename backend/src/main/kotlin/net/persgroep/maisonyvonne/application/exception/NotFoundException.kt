package net.persgroep.maisonyvonne.application.exception

open class NotFoundException(
    override val message: String? = null,
) : RuntimeException(message)
