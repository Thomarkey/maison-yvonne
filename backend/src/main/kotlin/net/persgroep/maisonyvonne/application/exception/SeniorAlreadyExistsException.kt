package net.persgroep.maisonyvonne.application.exception

open class SeniorAlreadyExistsException(override val message: String) : AlreadyExistsException(message)