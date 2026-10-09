# infrastructure/gateway/externalService

One outbound provider. Rename `externalService` to the real system and keep all
four pieces together:

    ExternalResource.kt       their HTTP contract, one-to-one (interface, no logic)
    FooGateway.kt             implements application/port, maps DTO <-> domain
    ExternalErrorDecoder.kt   their HTTP errors -> our exceptions
    api/                      their request/response DTOs

The decoder is the piece people forget. Feign throws `FeignException` on any
non-2xx, so without one every caller — service, controller, advice — would have
to understand Feign. With one, the edge is sealed: a 404 on `getFoo` comes out as
`NotFoundException`, anything else as `ExternalGatewayException`, and the
advice already knows how to map both.

- `methodKey` ("ExternalResource#getFoo(String,String)") is what tells you *which*
  call failed. A status alone has no business meaning.
- The decoder *returns* the exception, it does not throw it. Feign throws.
- One decoder per provider, never shared: 404 means something different in each
  API.
- Wired in `infrastructure/config/FeignConfiguration`, which also owns timeouts
  and retries — not the gateway.
