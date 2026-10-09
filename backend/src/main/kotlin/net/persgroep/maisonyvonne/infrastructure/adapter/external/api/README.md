# infrastructure/adapter/external/api

The event contract **as the other system sends it** — their field names, their
enums, their nullability. Don't improve it, mirror it.

Polymorphic events use a sealed/open base plus a type enum (`Event`,
`EventType`, `UpdatedEvent`) so the listener can `when (this)` over them.

These classes never leave this package: the listener converts them to domain
types before calling a service.

----

## Do / don't (from CLAUDE.md)

- **Do** read the publisher's actual documentation (or an existing consumer) for
  the event shape. Never guess a schema — ask for the docs if there are none.
- **Don't** give these classes behaviour. They carry the payload; mapping to
  domain happens in a `toDomain()`/`from` conversion and nothing else.
