package net.persgroep.maisonyvonne.application.service

import net.persgroep.maisonyvonne.application.command.CreateSeniorCommand
import net.persgroep.maisonyvonne.application.command.UpdateSeniorCommand
import net.persgroep.maisonyvonne.application.domain.Senior
import net.persgroep.maisonyvonne.application.exception.SeniorAlreadyExistsException
import net.persgroep.maisonyvonne.application.exception.SeniorNotFoundException
import net.persgroep.maisonyvonne.application.repository.SeniorRepository
import org.springframework.stereotype.Service

@Service
class SeniorService(
    val seniorRepository: SeniorRepository
) {

    fun getAllSeniors(): List<Senior> = seniorRepository.findAll()

    fun getSenior(id: String): Senior {
        return seniorRepository.getById(id = id)
            ?: throw SeniorNotFoundException("No senior found with seniorId $id")
    }

    fun createSenior(createSeniorCommand: CreateSeniorCommand): Senior {
        return validate(name = createSeniorCommand.name).let {
            Senior.create(createSeniorCommand = createSeniorCommand).also {
                seniorRepository.save(it)
            }
        }
    }

    fun updateSenior(updateSeniorCommand: UpdateSeniorCommand): Senior {
        val senior = getSenior(id = updateSeniorCommand.seniorId)
        if (senior.name != updateSeniorCommand.name) {
            validate(name = updateSeniorCommand.name)
        }
        return senior.update(updateSeniorCommand = updateSeniorCommand)
            .also { seniorRepository.save(it) }
    }

    // TODO cascade: this leaves the senior's visits orphaned in the visits collection
    fun deleteSenior(id: String) =
        getSenior(id = id).let {
            seniorRepository.deleteById(id)
        }

    private fun validate(name: String) {
        if (seniorRepository.existsByName(name = name)) {
            throw SeniorAlreadyExistsException("Senior already exists for name: $name")
        }
    }

}