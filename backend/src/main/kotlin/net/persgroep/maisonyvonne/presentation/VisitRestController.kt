package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.command.RegisterVisitCommand
import net.persgroep.maisonyvonne.application.command.UpdateVisitCommand
import net.persgroep.maisonyvonne.application.service.VisitService
import net.persgroep.maisonyvonne.presentation.api.VisitDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/visits")
class VisitRestController(
    val visitService: VisitService
) {

    @GetMapping
    fun getVisits(): ResponseEntity<List<VisitDTO>> {
        return ResponseEntity.ok(
            visitService.getAllVisits().map { VisitDTO.from(visit = it) }
        )
    }

    @PostMapping("/register")
    fun registerVisit(
        @RequestBody registerVisitCommand: RegisterVisitCommand
    ): ResponseEntity<VisitDTO> {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            VisitDTO.from(
                visitService.registerVisit(registerVisitCommand = registerVisitCommand)
            )
        )
    }

    @PutMapping
    fun updateVisit(
        @RequestBody updateVisitCommand: UpdateVisitCommand
    ): ResponseEntity<VisitDTO> {
        return ResponseEntity.ok(
            VisitDTO.from(
                visitService.updateVisit(updateVisitCommand = updateVisitCommand)
            )
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteVisit(
        @PathVariable id: String,
    ) {
        visitService.deleteVisit(id = id)
    }

}
