package net.persgroep.maisonyvonne.infrastructure.config.security

import java.time.ZoneId

class Constants {
    companion object {
        const val X_API_KEY = "X-Api-Key"
        const val X_USER_ID_HEADER = "x-user-id"
        val ZONE_ID: ZoneId = ZoneId.of("Europe/Brussels")
    }
}
