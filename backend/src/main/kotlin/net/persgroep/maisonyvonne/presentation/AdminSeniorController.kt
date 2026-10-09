package net.persgroep.maisonyvonne.presentation

import net.persgroep.maisonyvonne.application.service.SeniorService
import net.persgroep.maisonyvonne.presentation.api.SeniorDTO
import net.persgroep.maisonyvonne.presentation.api.SeniorRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/admin/seniors")
class AdminSeniorController(private val seniors: SeniorService) {

    @GetMapping
    fun seniors() = seniors.seniors().map { SeniorDTO.from(it) }

    @PostMapping
    fun create(@RequestBody request: SeniorRequest) =
        SeniorDTO.from(seniors.create(request.toCreateSeniorCommand()))

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody request: SeniorRequest) =
        SeniorDTO.from(seniors.update(request.toUpdateSeniorCommand(id)))
}
