# infrastructure/adapter

Inbound from other systems: SQS/SNS listeners, webhooks, scheduled pollers —
anything where the other side decides when we run.

One sub-package per source system (`adapter/external/`, `adapter/yuza/`), each
with an `api/` folder holding the event shapes that system sends us.

- A listener does three things: deserialize, dispatch to a service, log. No
  business rules in here.
- Map the event payload to domain types before handing it to a service.
- Decide explicitly whether a failure should rethrow (message goes back on the
  queue / DLQ) or be swallowed — that choice is the whole design of the listener.
- Expect redelivery. Handling must be idempotent.

----

## Do / don't (from CLAUDE.md)

- **Do** check event/side-effect pairing: if handling an event changes state,
  every path that changes that state emits the matching event, and vice versa.
  Mismatched pairs are the classic review finding here.
- **Don't** put business rules in a listener. Deserialize, map to domain,
  delegate to a service.
