# application/repository

Persistence interfaces, one per aggregate root. Spring Data does the
implementing, so there is no adapter class to write.

- Typed on the domain class: `MongoRepository<Foo, String>`. Documents and domain
  are the same shape here — if persistence ever needs its own shape, add an
  `infrastructure` document class with `from`/`toDomain` and keep the domain clean.
- Derived query methods (`findByNameAndStatus`) over `@Query` strings.
- Return `null` for "not there", and let the **service** decide whether that is a
  `FooNotFoundException` — a repository has no opinion on business meaning.

----

## Do / don't (from CLAUDE.md)

- **Do** keep Mongo annotations out of the domain if you follow the stricter
  project convention: persistence shapes live in `adapter/Documents.kt` as
  `@Document` classes with `from`/`toDomain`, and the domain stays annotation-free.
  (The skeleton's `Foo` annotates the domain class directly — kadoki's style.
  Pick one per project and be consistent.)
- **Do** store ids as strings so collections stay readable in Compass.
- **Don't** put business meaning on a missing row. Return `null`; the service
  decides whether that is an exception.
