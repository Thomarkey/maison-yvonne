# application/service

Where business policy and orchestration live. A service decides *when* a rule
applies and chains domain operations explicitly:

    fooRepository.findById(id)
        ?.activate()
        ?.also { fooRepository.save(it) }
        ?: throw FooNotFoundException(id)

- Takes commands (writes) or plain parameters (reads), returns **domain** objects.
  Mapping to DTOs happens in the controller, never here.
- Talks to the outside only through `application/port` and `application/repository`.
- Domain objects stay dumb on purpose: a domain method must not secretly trigger
  another business rule. If two things have to happen, the service says so.
- Split by use case once a service gets fat (`CreateFooService`,
  `UpdateFooService`) rather than growing one 600-line `FooService`.
- Sub-package a capability with its own technical surface (`service/pdf/`,
  `service/images/`) so it doesn't leak into every other service.

----

## Do / don't (from CLAUDE.md)

- **Do** keep policy here: the service decides when a rule applies and chains the
  domain operations explicitly.
- **Do** prefer Kotlin's null-safe chaining (`?.let`, `?:`, `.also`) over
  `if (x == null)` branches and intermediate variables.
- **Do** prefer `filter` over `mapNotNull` + `firstOrNull`:
  `list.filter { it.id in ids && it.active }`, not
  `ids.mapNotNull { id -> list.firstOrNull { it.id == id } }`.
- **Don't** enrich, filter or transform DTOs. Work with domain models and
  introduce an intermediate domain model (e.g. `EnrichedPaymentSource`) when a
  result combines several domain concepts.
- **Don't** use `!!` to get past a nullable repository result — throw the
  business exception instead.
