# infrastructure

The outside. Everything that knows about another system: HTTP clients, queues,
AWS SDKs, Spring wiring, serialization.

    adapter/   inbound — something else calls us without asking (events, queues)
    config/    Spring @Configuration: beans, security, clients, scheduling
    gateway/   outbound — we call something else; implements application/port
    util/      tiny technical helpers, no business meaning

Direction is the whole point: `infrastructure` imports from `application`, never
the other way around. A gateway maps its DTOs to domain types with `from`
factories on the DTO, so the domain stays unaware this package exists.

adapter vs gateway: who initiates. An SQS listener is an adapter, a Feign client
calling that same service is a gateway. Both can exist for one provider.

----

## Do / don't (from CLAUDE.md)

- **Do** put the conversion on the infrastructure type: `from` as a companion on
  the DTO/document (`PaymentSourceDTO.from(paymentSource)`), so the dependency
  points infrastructure → domain.
- **Do** read the actual API documentation or the existing client code before
  assuming an endpoint shape. Never guess a schema.
- **Don't** let anything in here leak upward. If `application` needs a type
  defined in this package, the port is wrong.
