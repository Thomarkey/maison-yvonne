# application/command

Input objects for write operations. One command per use case, named after the use
case (`CreateFooCommand`, `UpdateFooCommand`).

A command is the merge point between request data and caller identity: the
presentation DTO owns the `toXxxCommand()` conversion that stitches body fields
together with principal fields (brand, accountId, userId). That way a service
signature never grows a long list of loose parameters, and the controller cannot
forget to pass the principal.

- Plain `data class`, no behaviour, no framework annotations.
- Only for POST/PUT/PATCH-style operations. A read that needs nothing but
  principal fields takes plain parameters instead — don't wrap it in a command.
- Never carry a value that is already derivable from another field.

----

## Do / don't (from CLAUDE.md)

- **Do** use command objects for POST/PUT endpoints where request body data gets
  merged with principal data (brand, accountId). The **DTO** owns the
  `toXxxCommand()` conversion that stitches body fields and principal fields.
- **Don't** wrap a GET in a command. If it only uses principal fields, pass the
  parameters straight to the service.
- **Don't** put a field on a command that is already derivable from another field
  on it — callers can then pass two values that contradict each other.
