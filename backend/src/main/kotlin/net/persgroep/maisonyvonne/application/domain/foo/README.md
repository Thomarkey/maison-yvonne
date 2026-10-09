# application/domain/foo

One folder per aggregate. Everything that only makes sense together with `Foo`
lives here: the aggregate root, its value objects, its enums, its state.

Keeps `domain/` navigable once there are twenty types instead of five, and makes
the aggregate boundary visible — if a class in here is used by three other
aggregates, it probably belongs one level up in `domain/`.
