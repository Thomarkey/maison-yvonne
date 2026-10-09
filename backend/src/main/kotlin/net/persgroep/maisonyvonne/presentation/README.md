# presentation

Inbound HTTP. Controllers under `/api`, their DTOs in `api/`, and the single
`ExceptionControllerAdvice` that turns business exceptions into status codes.

    FooRestController.kt          one controller per resource
    ExceptionControllerAdvice.kt  exception -> HTTP status, in one place
    HealthController.kt
    api/                          request/response DTOs + ExceptionResult

- A controller is three lines per endpoint: DTO -> command, call the service, map
  the result to a DTO. No rules, no `if` on business state, no repository calls.
- Mapping happens **here** and nowhere deeper: services speak domain.
- Read the caller's identity from the principal. Never accept a userId, accountId
  or brand that the principal already carries — a second source can contradict it
  and lets a caller act on someone else's data.
- No `@RequestBody` on a GET. Inputs go in `@RequestParam`, `@PathVariable` or the
  principal.
- Never handle an exception in a controller: throw from the service and map it in
  the advice, so every endpoint answers the same way.

----

## Do / don't (from CLAUDE.md)

- **Do** keep backend routes under `/api`; everything else falls through to the
  frontend bundle (public site on `/`, pilot app on `/app`).
- **Do** use a command object for POST/PUT: the DTO's `toXxxCommand()` merges body
  fields with principal fields. For a GET that only needs principal fields, pass
  the parameters straight to the service.
- **Do** resolve the caller's own `links` first and then query only those senior
  ids with `publishedAt != null` for family reads.
- **Do** mark temporary backwards-compat routes with `@Deprecated` saying why they
  exist and when they can go, e.g.
  `@Deprecated("temp backwards-compat route for fugu, which still calls GET /api/payment-sources")`,
  so the cleanup branch knows exactly what to delete.
- **Don't** put a `@RequestBody` in a GET endpoint. Use `@RequestParam`,
  `@PathVariable` or the principal.
- **Don't** accept a value the principal already carries (`accountId`, brand) as a
  path variable, param or body field.
- **Don't** add a family endpoint that takes a senior id from the request.
