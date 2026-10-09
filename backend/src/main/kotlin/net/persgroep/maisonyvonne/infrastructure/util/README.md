# infrastructure/util

Small technical helpers with no business meaning: date formatting in the right
zone, SQS envelope unwrapping, string generation, resource loading.

- Extension functions on types we don't own (`Instant`, `String`, SDK classes) —
  that is what extensions are for. Logic about our own types belongs **on** those
  types, not here.
- One file per receiver type (`InstantUtil.kt`, `StringGenerator.kt`), so there is
  an obvious place to look before writing a new helper. Look first: a duplicate
  helper is how two copies of a rule start drifting.
- If a helper needs a domain type, it is not a util — it is a service.

----

## Do / don't (from CLAUDE.md)

- **Do** search this folder (and the codebase) before writing a helper. If it
  exists, reuse it and say so — "this already exists in X".
- **Do** reserve extension functions for classes we don't own. Logic about our
  own types belongs on those types.
- **Don't** copy a helper into a second place. Two copies drift, and the bug
  shows up only in the one nobody updated.
