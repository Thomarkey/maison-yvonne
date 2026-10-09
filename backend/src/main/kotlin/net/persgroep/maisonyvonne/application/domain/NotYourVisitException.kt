package net.persgroep.maisonyvonne.application.domain

import java.util.UUID

/** An admin tried to edit a visit someone else registered. */
class NotYourVisitException(visitId: UUID) : RuntimeException("visit $visitId was registered by another admin")
