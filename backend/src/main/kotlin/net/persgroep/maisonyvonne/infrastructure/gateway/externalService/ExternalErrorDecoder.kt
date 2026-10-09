package net.persgroep.maisonyvonne.infrastructure.gateway.externalService

import feign.Response
import feign.codec.ErrorDecoder
import net.persgroep.maisonyvonne.application.exception.ExternalGatewayException
import net.persgroep.maisonyvonne.application.exception.NotFoundException
import org.apache.commons.io.IOUtils
import org.springframework.stereotype.Component
import java.nio.charset.Charset

/**
 * The translation layer at the edge: provider HTTP status -> our own exception.
 * Without it a 404 surfaces as a FeignException and every caller would have to
 * know what Feign is. One decoder per provider, wired in FeignConfiguration.
 */
@Component
class ExternalErrorDecoder : ErrorDecoder {

    override fun decode(methodKey: String, response: Response): Exception {
        val responseBody = IOUtils.toString(response.body().asReader(Charset.defaultCharset()))
        return when (response.status()) {
            // methodKey is "ExternalResource#getFoo(String,String)" — the only way
            // to tell which call 404'd, since the status alone has no meaning
            404 -> when {
                methodKey.contains("getFoo") -> NotFoundException(responseBody)
                else -> ExternalGatewayException(responseBody)
            }

            else -> ExternalGatewayException(responseBody)
        }
    }
}
