package net.persgroep.maisonyvonne.infrastructure.util

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.stereotype.Component
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.readValue

@Component
class SqsUtil(
    private val jsonMapper: JsonMapper
) {

    fun <R> mapMessage(notificationMessage: String, clazz: Class<R>): R? =
        jsonMapper.readValue<Map<String, Any?>>(notificationMessage)["Message"]
            ?.takeIf { it.toString().isNotEmpty() }
            ?.let { jsonMapper.readValue(it.toString(), clazz) }

    data class MessageAttribute(
        @param:JsonProperty("Type")
        val type: String,
        @param:JsonProperty("Value")
        val value: String
    )

}