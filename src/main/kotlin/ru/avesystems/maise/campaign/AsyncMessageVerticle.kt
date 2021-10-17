package ru.avesystems.maise.campaign

import com.rabbitmq.client.ConnectionFactory
import io.reactivex.Completable
import io.vertx.reactivex.core.AbstractVerticle
import ru.avesystems.maise.campaign.domain.events.AbstractDomainEvent
import java.nio.charset.StandardCharsets

/**
 * The verticle to send async messages to other services.
 */
class AsyncMessageVerticle : AbstractVerticle() {
    override fun rxStart(): Completable {
        val eventBus = vertx.eventBus()

        eventBus.consumer<Any>("campaigns.events.occur") { message ->
            val mqUser = config().getString("RABBITMQ_USER")
            val mqPwd = config().getString("RABBITMQ_PASS")
            val mqHost = config().getString("RABBITMQ_HOST")
            val factory = ConnectionFactory()
            factory.host = mqHost
            factory.username = mqUser
            factory.password = mqPwd

            val event = message.body()

            if (event !is AbstractDomainEvent) {
                return@consumer
            }

            factory.newConnection().use { connection ->
                connection.createChannel().use { channel ->
                    val queueName = "campaign_msgs"

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
        }

        return Completable.complete()
    }
}