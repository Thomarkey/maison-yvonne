package net.persgroep.maisonyvonne.infrastructure.config

import feign.DefaultRetryer
import feign.Feign
import feign.Logger.JavaLogger
import feign.Request
import feign.RequestTemplate
import feign.httpclient.ApacheHttpClient
import feign.jackson3.Jackson3Decoder
import feign.jackson3.Jackson3Encoder
import net.persgroep.maisonyvonne.infrastructure.gateway.externalService.ExternalErrorDecoder
import net.persgroep.maisonyvonne.infrastructure.gateway.externalService.ExternalResource
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.support.SpringMvcContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import tools.jackson.databind.json.JsonMapper
import java.util.concurrent.TimeUnit

/**
 * Turns each `*Resource` interface into a client bean. One `@Bean` per provider,
 * one shared builder: timeouts, retries and (de)serialization are decided once
 * here instead of per gateway.
 */
@Configuration
class FeignConfiguration(
    private val jsonMapper: JsonMapper,
) {

    @Bean
    fun externalResource(
        @Value($$"${external.url}") url: String,
        externalErrorDecoder: ExternalErrorDecoder,
    ): ExternalResource =
        defaultBuilder()
            .contract(SpringMvcContract())
            .errorDecoder(externalErrorDecoder)
            .target(ExternalResource::class.java, url)

    private fun defaultBuilder(): Feign.Builder =
        Feign.builder()
            .client(ApacheHttpClient())
            .requestInterceptor { template: RequestTemplate ->
                template.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            }
            .encoder(Jackson3Encoder(jsonMapper))
            .decoder(Jackson3Decoder(jsonMapper))
            .logger(JavaLogger("Feign"))
            .options(Request.Options(30, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, true))
            .retryer(DefaultRetryer(100, 1000, 3))
}
