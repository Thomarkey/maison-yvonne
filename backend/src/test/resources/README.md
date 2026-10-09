# src/test/resources

Config for the test run only. Same overlay idea as the main profiles, except the
profile exists to *switch the outside world off*.

    application-IntegrationTests.yaml   profile used by AbstractIntegrationTest

What an integration-test profile is for:

- Disable anything that would reach a real system (`spring.cloud.aws.sqs.enabled:
  false`), so a test never consumes a real queue.
- Point persistence at the Testcontainers instance; the container's dynamic port
  is injected from the test base class, not hardcoded here.
- Give the `$${...}` keys the code requires a harmless dummy value — a missing key
  fails the whole context, and then every test in the module fails with the same
  unrelated error.

Test fixtures (json payloads a gateway test feeds to a mock server) live here too,
next to this file, named after the test that reads them.

----

## Do / don't (from CLAUDE.md)

These apply to the whole test source set, not just this folder.

- **Do** run the full test suite immediately after changing tested code and fix
  broken mocks/return values before presenting the result.
- **Do** look at sibling test classes in the same package first and follow their
  conventions (how dependencies are mocked, how data is built, how assertions are
  structured). Consistency beats any single "correct" style.
- **Do** use randomized test data from the `DomainMother` helpers (`anInvoice()`,
  `aTransactionForSingleVideo()`) instead of hardcoded values, and interpolate the
  object's own fields into expected output (`"id": "${invoice.id}"`) so the test
  checks structure and wiring rather than magic values.
- **Do** import `@WebMvcTest` from `org.springframework.boot.webmvc.test.autoconfigure`
  (Boot 4 split the test slices into their own modules, via
  `spring-boot-starter-webmvc-test`).
- **Don't** use `any()` when stubbing or verifying a call that persists or mutates
  state (`save(any())` proves nothing). Build the expected object and match it,
  e.g. `refEq(expectedSavedOffer, "id", "receivedOn")` to exclude generated
  fields, reusing the same variable in the stub and the verify.
- **Don't** leave a `!!` on a receiver that is already non-null. `!!` is fine in
  test code only, and only where the receiver really is nullable there.
