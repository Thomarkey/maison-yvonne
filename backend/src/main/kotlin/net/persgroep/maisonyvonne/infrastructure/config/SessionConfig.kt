package net.persgroep.maisonyvonne.infrastructure.config

import org.mongodb.spring.session.config.annotation.web.http.EnableMongoHttpSession
import org.springframework.context.annotation.Configuration

/** Sessions live in the `sessions` collection, so restarts and redeploys don't log anyone out. */
@Configuration
@EnableMongoHttpSession(maxInactiveIntervalInSeconds = 30 * 24 * 60 * 60)
class SessionConfig
