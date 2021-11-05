package ru.avesystems.maise.campaign

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import io.reactivex.Completable
import io.vertx.reactivex.core.AbstractVerticle
import ru.avesystems.maise.campaign.domain.events.AbstractDomainEvent
import java.nio.charset.StandardCharsets

/**
 * The verticle to send async messages to other services.
 */
class AsyncMessageVerticle : AbstractVerticle() {
    private val queueName = "campaign_msgs"

    override fun rxStart(): Completable {
        val eventBus = vertx.eventBus()

        eventBus.consumer<Any>("campaigns.events.occur") { message ->
            val event = message.body()

            if (event !is AbstractDomainEvent) {
                return@consumer
            }

            connectToMQ().use { channel ->
                sendEventToQueue(event, channel)
            }
        }

        return Completable.complete()
    }

    /**
     * Connects to the mq server using the config for the service.
     */
    private fun connectToMQ(): Channel {
        val mqUser = config().getString("RABBITMQ_USER")
        val mqPwd = config().getString("RABBITMQ_PASS")
        val mqHost = config().getString("RABBITMQ_HOST")
        val factory = ConnectionFactory()
        factory.host = mqHost
        factory.username = mqUser
        factory.password = mqPwd

        val connection = factory.newConnection()
        return connection.createChannel()
    }

    /**
     * Sends the event to the queue of async messages
     */
    private fun sendEventToQueue(event: AbstractDomainEvent, channel: Channel) {
        channel.queueDeclare(queueName, false, false, false, null)

        val eventData = event.toJson()
        eventData.put("type", event.type)

        val asyncMsg = eventData.toString()

        channel.basicPublish(
            "",
            queueName,
            null,
            asyncMsg.toByteArray(StandardCharsets.UTF_8)
        )
    }
}