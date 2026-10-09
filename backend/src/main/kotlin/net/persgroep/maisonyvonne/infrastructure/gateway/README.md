# infrastructure/gateway

Outbound to other systems. Each sub-package is one provider and contains:

    externalService/
      ExternalResource.kt       the raw client interface (Feign/RestClient), their endpoints
      FooGateway.kt             implements an application/port, maps DTO <-> domain
      ExternalErrorDecoder.kt   their HTTP errors -> our exceptions
      api/                      the provider's request/response DTOs

- The `*Gateway` class is the only thing the application sees, and it implements a
  port. The `*Resource` interface stays package-private in spirit: nothing outside
  this package calls it.
- Never let a provider failure escape as a provider type. The `ErrorDecoder`
  turns non-2xx into exceptions from `application/exception`, so no caller ever
  catches a `FeignException`.
- Conversion sits on the DTO as `from`/`toDomain` companions, so the domain keeps
  no import pointing here.
- Client beans, timeouts and retries live in `infrastructure/config/FeignConfiguration`,
  one `@Bean` per provider — not in the gateway.
- Provider-specific SDK extras (Chargebee/Stripe-style SDK objects) go in an
  `extensions/` sub-package as Kotlin extension functions — extending types we
  don't own is exactly what extensions are for.

----

## Do / don't (from CLAUDE.md)

- **Do** read the real API docs or existing client code before writing a
  `*Resource` interface. Never guess endpoint or payload shapes.
- **Do** use Kotlin extension functions for types you don't own — Chargebee,
  Stripe, AWS SDK objects — and keep them in an `extensions/` sub-package.
- **Do** put mapping on the DTO as `from`/`toDomain`, keeping the domain free of
  imports from here.
- **Don't** add an extension function for one of our own types; add the method to
  the type instead.
