package net.persgroep.maisonyvonne.application.service

import net.persgroep.maisonyvonne.application.command.RegisterVisitCommand
import net.persgroep.maisonyvonne.application.command.UpdateVisitCommand
import net.persgroep.maisonyvonne.application.domain.Visit
import net.persgroep.maisonyvonne.application.exception.VisitNotFoundException
import net.persgroep.maisonyvonne.application.repository.VisitRepository
import org.springframework.stereotype.Service

@Service
class VisitService(
    val visitRepository: VisitRepository,
    val seniorService: SeniorService,
) {

    fun getAllVisits() = visitRepository.findAll()

    fun getVisit(id: String): Visit {
        return visitRepository.getById(id = id)
            ?: throw VisitNotFoundException("No visit found with visitId $id")
    }

    fun registerVisit(registerVisitCommand: RegisterVisitCommand): Visit {
        seniorService.getSenior(id = registerVisitCommand.seniorId)
        return Visit.create(registerVisitCommand = registerVisitCommand).also {
            visitRepository.save(it)
        }
    }

    fun updateVisit(updateVisitCommand: UpdateVisitCommand) =
        getVisit(id = updateVisitCommand.visitId)
            .update(updateVisitCommand = updateVisitCommand)
            .also { visitRepository.save(it) }

    fun deleteVisit(id: String) =
        getVisit(id = id).let {
            visitRepository.deleteById(id)
        }

}