# infrastructure/config

Spring `@Configuration` classes — the wiring, one file per concern:
`JsonMapperConfig`, `FeignConfiguration`, `MongoConfig`, `SecurityConfig`,
`ScheduleConfig`, `SecretsConfig`.

- Beans and property binding only. A `@Configuration` that contains an `if` about
  business rules is a service in disguise.
- `@Value`/`@ConfigurationProperties` resolve here and get passed into gateways as
  constructor parameters, so nothing deeper reads the environment. Every key
  referenced here must exist in `application.yaml` **and** in each profile
  overlay, or that environment fails at startup.
- `FeignConfiguration` builds one client bean per provider from a shared builder:
  timeouts, retries, encoder/decoder and the provider's `ErrorDecoder` are
  decided once here, never per gateway.
- Per-brand / per-tenant settings belong in a config class too, not scattered
  across services.

----

## Do / don't (from CLAUDE.md)

- **Do** use Jackson 3: `JsonMapper` from `tools.jackson.databind.json.JsonMapper`.
- **Do** name the variable and parameter `jsonMapper`.
- **Don't** use the legacy `ObjectMapper` from `com.fasterxml.jackson.databind`,
  and don't call anything `objectMapper`.
