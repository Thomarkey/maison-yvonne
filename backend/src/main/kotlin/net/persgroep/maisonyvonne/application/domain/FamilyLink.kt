package net.persgroep.maisonyvonne.application.domain

import java.util.UUID

/** A family account may read [seniorId]'s updates; [relationLabel] is e.g. "dochter". */
data class FamilyLink(val seniorId: UUID, val relationLabel: String?)
