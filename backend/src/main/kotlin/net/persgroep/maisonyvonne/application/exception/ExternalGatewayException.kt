package net.persgroep.maisonyvonne.application.exception

/**
 * "The other system failed and we have no business meaning for it." One per
 * provider, thrown by its error decoder, mapped to 500 by the advice.
 */
class ExternalGatewayException(
    override val message: String? = null,
) : RuntimeException(message)
