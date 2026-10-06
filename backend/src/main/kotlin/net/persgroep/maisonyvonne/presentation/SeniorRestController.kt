package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.command.CreateSeniorCommand
import net.persgroep.maisonyvonne.application.command.UpdateSeniorCommand
import net.persgroep.maisonyvonne.application.service.SeniorService
import net.persgroep.maisonyvonne.presentation.api.SeniorDTO
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/seniors")
class SeniorRestController(
    val seniorService: SeniorService
) {

    @GetMapping
    fun getSeniors(): ResponseEntity<List<SeniorDTO>> {
        return ResponseEntity.ok(
            seniorService.getAllSeniors().map { SeniorDTO.from(senior = it) }
        )
    }

    @PostMapping
    fun createSenior(
        @RequestBody createSeniorCommand: CreateSeniorCommand
    ): ResponseEntity<SeniorDTO> {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            SeniorDTO.from(
                seniorService.createSenior(createSeniorCommand = createSeniorCommand)
            )
        )
    }

    @PutMapping
    fun updateSenior(
        @RequestBody updateSeniorCommand: UpdateSeniorCommand
    ): ResponseEntity<SeniorDTO> {
        return ResponseEntity.ok(
            SeniorDTO.from(
                seniorService.updateSenior(updateSeniorCommand = updateSeniorCommand)
            )
        )
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteSenior(
        @PathVariable id: String,
    ) {
        seniorService.deleteSenior(id = id)
    }

}