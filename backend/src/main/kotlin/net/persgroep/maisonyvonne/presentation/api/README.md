# presentation/api

Our own HTTP contract: request and response DTOs, plus the error body.

- Response DTO: `FooDTO.from(foo)` companion, called by the controller.
- Request DTO: `toFooCommand(userId, brand)` — it merges body fields with
  principal fields and hands back a command.
- Pure data carriers. No enrichment, no filtering, no lookups. If a response
  combines several domain concepts, build an intermediate **domain** model
  (`EnrichedFoo`) in the service and map that here.
- Validation annotations (`@field:NotBlank`) belong on the request DTO — reject
  nonsense at the boundary instead of guarding for it three layers deep.

----

## Do / don't (from CLAUDE.md)

- **Do** let a DTO depend on domain types (`PaymentSourceDTO.from(PaymentSource)`).
  The reverse is forbidden: a domain type must never know about a DTO.
- **Do** own the `toXxxCommand()` conversion on the request DTO — that is where
  body fields and principal fields get stitched together.
- **Don't** perform business logic on a DTO. No enriching, filtering or
  transforming: do it on domain models in the service, add an intermediate domain
  model if the result combines several concepts, and map here at the very end.
