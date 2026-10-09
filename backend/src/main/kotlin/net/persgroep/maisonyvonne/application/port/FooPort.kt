package net.persgroep.maisonyvonne.application.port

import net.persgroep.maisonyvonne.application.domain.foo.Foo

interface FooPort {

    fun register(foo: Foo): String

    fun findExternalFoo(externalId: String): Foo?
}
