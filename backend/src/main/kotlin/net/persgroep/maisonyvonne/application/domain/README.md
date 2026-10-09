# application/domain

The model. Entities, value objects, enums, and the small operations that belong
to them.

    AbstractBase.kt   reflection equals/toString for value comparison
    foo/              one folder per aggregate
    security/         the authenticated caller

- `val` everywhere. A change produces a new instance (`copy(...)`), which is why
  reflection-based equality is safe here.
- Domain methods are small and self-contained: they act on their own state and
  return the result. They never reach out to a repository, a port, or another
  aggregate's rules — the service chains that explicitly.
- No framework in the signatures. Persistence annotations are tolerated on the
  document shape; the moment the domain needs to know about HTTP, a DTO or an SDK
  type, something is in the wrong package.
- Model constraints in the **type**: a non-nullable field, an enum instead of a
  String, a `Set` instead of a `List` of ids. A compile-time guarantee beats a
  runtime check in a service.

----

## Do / don't (from CLAUDE.md)

- **Do** default to `val`. Only use `var` when something genuinely reassigns the
  property later — a value set once at construction, including one deserialized
  from JSON, is a `val`. When you touch a `var`, check for an actual write and
  flip it if there is none.
- **Do** use the type system instead of runtime checks: non-nullable where null
  cannot happen, an enum instead of a String, and a `Set` instead of a `List`
  when the values are ids (unique, unordered, O(1) `in`).
- **Do** encapsulate reusable logic on the type itself — a property or method on
  the class/enum, not a repeated expression at three call sites.
- **Don't** give a domain method a second job. It operates on its own state and
  returns the result; the service chains anything further.
- **Don't** let a domain class know about a DTO, infrastructure or presentation
  type. Dependencies flow infrastructure → domain, never the reverse.
