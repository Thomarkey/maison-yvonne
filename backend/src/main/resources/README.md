# src/main/resources

Everything the jar carries that is not code.

    application.yaml              base config — all environments, no secrets
    application-<profile>.yaml    overlay per profile (acc, prod, dev, local)
    logback-spring.xml            logging, per profile
    templates/                    html/css/fonts/images for generated documents
    static/                       (here: the built frontend bundle, see build.gradle.kts)

## Why this folder matters to the Kotlin tree

Every `$${...}` in the code resolves to a key in here. They come in pairs:

| code | key |
|---|---|
| `@SqsListener([$$"${sqs.external-events-queue}"])` in `adapter/external` | `sqs.external-events-queue` |
| `@Value($$"${external.api-key}")` in `gateway/external` | `external.api-key` |
| `@Value($$"${external.url}")` on the HTTP client config | `external.url` |

So when a gateway or listener is added, this file changes in the same commit —
and in **every** profile overlay, or that environment fails at startup, not at
first call. Nothing reads the environment except `infrastructure/config` and the
gateway constructors it feeds.

## Rules

- Base file holds the shape and the safe defaults. The overlay holds only what
  differs: urls, queue names, secret names. Never duplicate the whole file.
- No secrets. Config holds the *name* of a secret
  (`secretName: acc/maison-yvonne/external`) and the secrets manager resolves it
  at startup via `spring.config.import`.
- Queue and topic names are environment-suffixed (`...-queue-acc`), so a wrong
  profile cannot read production's queue.
- Property names are kebab-case; they bind to camelCase Kotlin properties.

----

## Do / don't (from CLAUDE.md)

- **Do** put Mongo connection settings under `spring.mongodb.*`. Boot 4 renamed
  them from `spring.data.mongodb.*`, which now binds to **nothing** — you
  silently end up in the `test` database. `spring.data.mongodb.auto-index-creation`
  is the exception and kept its old prefix.
- **Do** keep the frontend bundle out of git: the build writes to
  `backend/build/frontend/static` (wired in `vite.config.ts` and
  `backend/build.gradle.kts`). Never commit build output.
- **Don't** store a secret here. Config holds the secret's *name*; the secrets
  manager resolves it at startup.
