# net.persgroep.maisonyvonne

Hexagonal layout. Three top packages, dependencies point **inward only**:

    presentation  ──┐
                    ├──> application ──> application/domain
    infrastructure ─┘

    application/        the inside: business rules, no framework-facing I/O
      command/          input objects for write operations
      domain/           the model itself, grouped per aggregate (domain/foo/)
      exception/        business exceptions, mapped to HTTP in presentation
      port/             interfaces the application needs from the outside world
      repository/       persistence interfaces (Spring Data)
      service/          orchestration: the only place policy lives
    infrastructure/     the outside: everything that talks to another system
      adapter/          inbound from other systems (queue/event listeners)
      config/           Spring @Configuration, beans, security wiring
      gateway/          outbound to other systems, implements application/port
      util/             tiny technical helpers with no business meaning
    presentation/       inbound HTTP: controllers + DTOs

Rules that matter more than the folders:

- `application` must never import from `infrastructure` or `presentation`.
- A port is declared in `application/port` and implemented in `infrastructure/gateway`.
  The application never sees the DTOs or SDK types behind it.
- DTOs (presentation and gateway) are pure data carriers. Conversion lives on them
  (`toFooCommand()`, `FooDTO.from(foo)`), never business logic.
- Every folder here has a README stating what belongs in it. Keep them honest.

Everything in this tree is a **dummy example** (`Foo`, `Event`) kept as a reference
skeleton. It is not meant to compile or run.

Config lives next door, not in here: every `$${...}` in `infrastructure` resolves
to a key in `src/main/resources/application.yaml` plus its per-profile overlay.
See `src/main/resources/README.md` for which key backs which class.

----

## Do / don't (from CLAUDE.md)

- **Do** keep dependencies pointing inward only: `infrastructure`/`presentation`
  → `application` → `application/domain`. A domain class must never import
  infrastructure, presentation or a DTO.
- **Do** verify the full wiring chain after touching a layer: controller →
  service → port → adapter. A change is not done until the chain is connected.
- **Do** pick the conversion direction that keeps the domain clean: `to` on the
  source (`domainObject.toDto()`), `from` as a factory on the target
  (`LegalBasis.from(reason)`). Prefer `from` on infrastructure types.
- **Don't** pass a value that another parameter already carries or derives
  (`PlanVO.legacy(externalPlan)`, not `legacy(externalPlan, externalReferenceId)`).
  A redundant parameter lets two callers disagree silently.
- **Don't** use `!!` in production code. A nullable type means null happens:
  throw something meaningful, return a default, or short-circuit with `?.let ?: `.
- **Don't** duplicate logic. Search for an existing helper/extension/service
  first and say so — "this already exists in X, we should reuse it".
- **Don't** leave IDE warnings in a file you touched (unused imports, unnecessary
  non-null assertions).
