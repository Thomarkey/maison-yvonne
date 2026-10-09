# application/port

Interfaces for everything the application needs from the outside world: another
service, a mail sender, a PDF renderer, a clock. The "ports" of ports & adapters.

- Declared **in terms of domain types only**. No DTOs, no SDK types, no HTTP or
  queue vocabulary — the application must not be able to tell whether the
  implementation is REST, SQS or an in-memory stub.
- Named after the capability, not the provider: `PaymentPort`, not `StripePort`.
  The implementation in `infrastructure/gateway/<provider>` carries the name.
- Implemented in `infrastructure/gateway`. Persistence is the exception: it goes
  through `application/repository` instead.

`port` implementations that touch infrastructure (DB, HTTP clients, queues).
Map infrastructure types to domain types with `from` factories, never the reverse.
----

## Do / don't (from CLAUDE.md)

- **Do** declare ports in domain terms only, so the application cannot tell what
  is behind them.
- **Do** check each parameter of a new signature against the others: if one is a
  field of another, drop it.
- **Don't** create an interface with one implementation just to have a port. A
  port exists because the application must not see the outside type, not as
  decoration.
