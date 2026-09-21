package net.persgroep.maisonyvonne.application.domain

/**
 * What one family member sees for one senior: the senior and their most recent
 * published visit ([latestVisit] is null before the first visit is published).
 */
data class SeniorUpdate(
    val senior: Senior,
    val relationLabel: String?,
    val latestVisit: Visit?,
)
