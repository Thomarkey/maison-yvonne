package net.persgroep.maisonyvonne.presentation.api

import net.persgroep.maisonyvonne.application.domain.Senior
import java.util.*

data class SeniorDTO(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val nickName: String? = null,
    val visits: Set<VisitDTO>? = emptySet(),
) {
    companion object {
        fun from(senior: Senior) = SeniorDTO(
            id = senior.id,
            name = senior.name,
            nickName = senior.nickname,
            visits = senior.visits?.map { VisitDTO.from(it) }?.toSet()
        )
    }
}