package net.persgroep.maisonyvonne.application.exception

class FooNotFoundException(fooId: String) : NotFoundException(message = "Foo $fooId not found")
