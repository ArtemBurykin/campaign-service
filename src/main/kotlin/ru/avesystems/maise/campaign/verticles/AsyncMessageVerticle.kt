package ru.avesystems.maise.campaign.verticles

import io.reactivex.Completable
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.serviceproxy.ServiceBinder
import ru.avesystems.maise.campaign.domain.events.AbstractDomainEvent
import ru.avesystems.maise.campaign.services.MQService
import ru.avesystems.maise.campaign.services.MQServiceFactory

/**
 * The verticle to send async messages to other services.
 */
class AsyncMessageVerticle : AbstractVerticle() {

    override fun rxStart(): Completable {
        val eventBus = vertx.eventBus()

        registerMQService()

        val service: MQService = MQServiceFactory.createProxy(vertx.delegate, "mq.service")

        eventBus.consumer<Any>("campaigns.events.occur") { message ->
            val event = message.body()

            if (event !is AbstractDomainEvent) {
                return@consumer
            }

            val eventData = event.toJson()
            eventData.put("type", event.type)
            val asyncMsg = eventData.toString()

            service.sendEventDataToQueue(asyncMsg)
        }

        return Completable.complete()
    }

    private fun registerMQService() {
        val service = MQServiceFactory.create(vertx.delegate)

        ServiceBinder(vertx.delegate)
            .setAddress("mq.service")
            .register(MQService::class.java, service)
    }
}