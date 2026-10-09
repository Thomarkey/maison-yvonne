# infrastructure/gateway/external/api

The provider's wire format: request and response DTOs, exactly as documented by
them. One class per payload, `data class`, nullable where they are nullable.

Conversion lives here, pointing inward only:

    FooDTO.from(foo)   // domain -> provider  (companion on the DTO)
    fooDTO.toDomain()  // provider -> domain  (method on the DTO)

So the DTO knows the domain and the domain knows nothing about the provider.
No business logic on a DTO — if a conversion needs a rule, the rule belongs in a
service and the DTO just carries the result.

----

## Do / don't (from CLAUDE.md)

- **Do** read the provider's real API documentation or existing client code
  before writing these. Never guess an endpoint or payload shape.
- **Do** keep conversion here as `from` on the DTO (`FooDTO.from(foo)`) and
  `toDomain()` — the DTO may depend on domain types, never the reverse.
- **Don't** perform business logic on a DTO. If a mapping needs a rule, the rule
  belongs in a service.
