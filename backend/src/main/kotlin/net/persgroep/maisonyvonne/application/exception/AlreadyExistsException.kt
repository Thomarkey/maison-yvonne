package net.persgroep.maisonyvonne.application.exception

open class AlreadyExistsException(override val message: String? = null) : RuntimeException(message)