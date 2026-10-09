package net.persgroep.maisonyvonne.application.service

import net.persgroep.maisonyvonne.application.command.CreateSeniorCommand
import net.persgroep.maisonyvonne.application.command.UpdateSeniorCommand
import net.persgroep.maisonyvonne.application.domain.Senior
import net.persgroep.maisonyvonne.application.repository.SeniorRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SeniorService(private val seniors: SeniorRepository) {

    fun seniors() = seniors.findAll()

    fun create(command: CreateSeniorCommand) =
        Senior(
            id = UUID.randomUUID(),
            preferredName = preferredNameOrFirstName(command.preferredName, command.fullName),
            fullName = command.fullName,
        ).also { seniors.save(it) }

    fun update(command: UpdateSeniorCommand): Senior {
        val existing = seniors.find(command.seniorId) ?: throw NoSuchElementException("unknown senior ${command.seniorId}")
        return existing.copy(
            preferredName = preferredNameOrFirstName(command.preferredName, command.fullName),
            fullName = command.fullName,
        ).also { seniors.save(it) }
    }

    /** No preferred name given: call them by the first name from their full name. */
    private fun preferredNameOrFirstName(preferredName: String?, fullName: String) =
        preferredName?.takeIf { it.isNotBlank() } ?: fullName.trim().substringBefore(' ')
}
