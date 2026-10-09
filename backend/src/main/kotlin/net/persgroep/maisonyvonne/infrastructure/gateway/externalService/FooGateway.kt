package net.persgroep.maisonyvonne.infrastructure.gateway.externalService

import net.persgroep.maisonyvonne.application.domain.foo.Foo
import net.persgroep.maisonyvonne.application.port.FooPort
import net.persgroep.maisonyvonne.infrastructure.gateway.externalService.api.FooDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FooGateway(
    private val externalResource: ExternalResource,
    @param:Value($$"${external.api-key}") private val apiKey: String,
) : FooPort {

    override fun register(foo: Foo): String =
        externalResource.createFoo(apiKey = apiKey, fooDTO = FooDTO.from(foo)).id

    override fun findExternalFoo(externalId: String): Foo? =
        externalResource.getFoo(apiKey = apiKey, fooId = externalId)?.toDomain()
}
