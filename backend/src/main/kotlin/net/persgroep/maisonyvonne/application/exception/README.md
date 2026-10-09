# application/exception

Business exceptions thrown by services and domain code. Named after what went
wrong in business terms (`FooNotFoundException`), never after the HTTP status.

- Keep a small set of open base types (`NotFoundException`, `BadRequestException`)
  and subclass them per case, so `ExceptionControllerAdvice` maps the base type
  once and new cases need no new handler.
- The message is what the caller gets to read: state the identifier that failed,
  never a stack-trace detail or an internal id scheme.
- Nothing framework-specific in here (no `ResponseStatusException`), otherwise
  `application` starts depending on the web layer.
