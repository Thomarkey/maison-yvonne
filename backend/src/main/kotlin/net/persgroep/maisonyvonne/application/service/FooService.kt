package net.persgroep.maisonyvonne.application.service

import net.persgroep.maisonyvonne.application.command.CreateFooCommand
import net.persgroep.maisonyvonne.application.domain.foo.Foo
import net.persgroep.maisonyvonne.application.exception.FooNotFoundException
import net.persgroep.maisonyvonne.application.port.FooPort
import net.persgroep.maisonyvonne.application.repository.FooRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class FooService(
    private val fooRepository: FooRepository,
    private val fooPort: FooPort,
) {
    companion object {
        private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun create(command: CreateFooCommand): Foo =
        Foo(
            id = UUID.randomUUID().toString(),
            name = command.name,
            amount = command.amount,
        ).let { foo ->
            fooPort.register(foo)
            fooRepository.save(foo)
        }.also { logger.info("Created foo ${it.id}") }

    fun retrieve(fooId: String): Foo =
        fooRepository.findById(fooId)
            .orElseThrow { FooNotFoundException(fooId) }

    fun activate(fooId: String): Foo =
        retrieve(fooId)
            .activate()
            .let { fooRepository.save(it) }
}
