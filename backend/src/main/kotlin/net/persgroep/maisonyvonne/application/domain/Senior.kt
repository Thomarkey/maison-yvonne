package net.persgroep.maisonyvonne.application.domain

import net.persgroep.maisonyvonne.application.command.CreateSeniorCommand
import net.persgroep.maisonyvonne.application.command.UpdateSeniorCommand
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document
data class Senior(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val nickname: String? = null,
    val visits: Set<Visit>? = emptySet()
) {
    companion object {
        fun create(createSeniorCommand: CreateSeniorCommand) = Senior(
            id = UUID.randomUUID().toString(),
            name = createSeniorCommand.name,
            nickname = createSeniorCommand.nickname
        )
    }

    fun update(updateSeniorCommand: UpdateSeniorCommand) =
        this.copy(name = updateSeniorCommand.name)
}