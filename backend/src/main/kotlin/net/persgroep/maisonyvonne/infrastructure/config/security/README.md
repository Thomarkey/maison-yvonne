# infrastructure/config/security

How a request becomes an authenticated principal: the filter that reads the
headers/session, the `Authentication` implementation, and the header name
constants.

- The principal type itself lives in `application/domain/security` — the
  application is allowed to know *who* is calling, not *how* that was decided.
- Whatever the principal carries (userId, brand, accountId) is the single source
  of truth. Never accept those again as a path variable, request param or body
  field: a duplicate lets a caller act on someone else's data.
- Constants (header names, zone id) live here so filters, gateways and controllers
  all spell them the same way.

----

## Do / don't (from CLAUDE.md)

- **Do** keep the auth mechanism here: session cookie + form login
  (`/api/auth/login`), CSRF via Spring's `csrf { spa() }` — the SPA echoes the
  `XSRF-TOKEN` cookie back as `X-XSRF-TOKEN`.
- **Don't** expose anything the principal already carries as a request
  parameter. Whatever the filter puts on the principal is the only source of
  truth for it.
