# application/domain/security

The authenticated caller, as a domain concept: who is calling and what they are
scoped to (userId, accountId, brand).

It lives in `domain` — not in `infrastructure/config/security` — because services
are allowed to know *who* is calling. The filter that builds it, the headers it
reads and the Spring `Authentication` plumbing stay in infrastructure.

Treat it as the single source of truth for those values: if it is on the
principal, never take it again from the request.

----

## Do / don't (from CLAUDE.md)

- **Do** read `accountId`, `brand`, `userId` from the authenticated principal
  (e.g. `FuguPrincipal`) inside the function that needs them.
- **Don't** accept a value the principal already carries as a path variable,
  request param or body field. A duplicate can contradict the principal and lets
  a caller act on someone else's data.
