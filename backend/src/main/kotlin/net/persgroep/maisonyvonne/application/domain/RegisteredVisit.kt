package net.persgroep.maisonyvonne.application.domain

/** A visit together with the display name of the admin who registered it (null if that account is gone). */
data class RegisteredVisit(
    val visit: Visit,
    val registeredBy: String?,
)
