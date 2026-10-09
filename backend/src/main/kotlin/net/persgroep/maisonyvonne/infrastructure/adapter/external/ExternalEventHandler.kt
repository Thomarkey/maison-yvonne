package net.persgroep.maisonyvonne.infrastructure.adapter.external

import net.persgroep.maisonyvonne.infrastructure.adapter.external.api.Event
import net.persgroep.maisonyvonne.infrastructure.adapter.external.api.UpdatedEvent

class ExternalEventHandler {

    @SqsListener(value = [$$"${sqs.external-events-queue}"])
    fun receive(@NotificationMessage notificationMessage: String) {
        logger.info("$notificationMessage")
        sqsUtil.mapMessage(notificationMessage, Event::class.java)?.run {
            when (this) {
                is UpdatedEvent -> {
                    updateFoo(foo = this)
                }
            }
        }
    }

    private fun foo(updatedEvent: UpdatedEvent) {
        //dosomething with updatedEvent
    }

}