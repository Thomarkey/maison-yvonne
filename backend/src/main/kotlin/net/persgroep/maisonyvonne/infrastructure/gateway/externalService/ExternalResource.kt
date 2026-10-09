package net.persgroep.maisonyvonne.infrastructure.gateway.externalService

import net.persgroep.maisonyvonne.infrastructure.config.security.Constants.Companion.X_API_KEY
import net.persgroep.maisonyvonne.infrastructure.gateway.externalService.api.FooDTO
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

/**
 * The provider's HTTP contract, one-to-one. Declared as an interface so the HTTP
 * client (Feign / @HttpExchange) is generated and the gateway stays testable.
 */
interface ExternalResource {

    @PostMapping("/api/foos")
    fun createFoo(
        @RequestHeader(X_API_KEY) apiKey: String,
        @RequestBody fooDTO: FooDTO,
    ): FooDTO

    @GetMapping("/api/foos/{fooId}")
    fun getFoo(
        @RequestHeader(X_API_KEY) apiKey: String,
        @PathVariable fooId: String,
    ): FooDTO?
}
