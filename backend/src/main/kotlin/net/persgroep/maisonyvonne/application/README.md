# application

The inside of the hexagon: business rules and the model they operate on. Knows
nothing about HTTP, queues, Mongo or any SDK — it must stay compilable with every
`infrastructure` and `presentation` class deleted.

    command/      input objects for write use cases
    domain/       the model, grouped per aggregate
    exception/    business exceptions
    port/         interfaces onto the outside world, in domain terms
    repository/   persistence interfaces
    service/      orchestration and policy — the only place rules get chained

Reading the flow: a controller turns a DTO into a **command**, a **service**
applies policy by calling small **domain** operations, and reaches outside only
through a **port** or a **repository**. Services return domain objects; the
controller maps them to DTOs.

The one import rule: nothing in here may import from `infrastructure` or
`presentation`.

----

## Do / don't (from CLAUDE.md)

- **Do** keep business policy and orchestration in application services. The
  service decides when a rule applies and chains domain operations explicitly.
- **Do** work with domain models throughout and map to DTOs only at the API
  boundary, in the controller.
- **Don't** import `infrastructure` or `presentation` from anywhere in here —
  that is the one rule the whole layout exists for.
- **Don't** let a domain object trigger business rules as a side effect. Prefer
  `blacklist(...).setPaymentSourceToSubscriptionIfMissing().also { save(it) }`
  in the service over `blacklist()` quietly doing the promotion itself.
