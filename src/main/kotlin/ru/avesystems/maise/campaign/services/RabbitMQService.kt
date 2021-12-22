package ru.avesystems.maise.campaign.services

import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import java.nio.charset.StandardCharsets

/**
 * The realization of the MQ Service using RabbitMQ.
 */
class RabbitMQService(private val vertx: Vertx) : MQService {
    private val queueName = "campaign_msgs"

    override fun sendEventDataToQueue(eventData: String) {
        connectToMQ().subscribe { channel ->
            sendEventToQueue(eventData, channel)
        }
    }

    /**
     * Connects to the mq server using the config for the service.
     */
    private fun connectToMQ(): Single<Channel> {
        val configRetriever = ConfigRetriever.create(vertx)

        val subject: SingleSubject<Channel> = SingleSubject.create()

        configRetriever.getConfig{ ar ->
            val config = ar.result()
            val mqUser = config.getString("RABBITMQ_USER")
            val mqPwd = config.getString("RABBITMQ_PASS")
            val mqHost = config.getString("RABBITMQ_HOST")
            val factory = ConnectionFactory()
            factory.host = mqHost
            factory.username = mqUser
            factory.password = mqPwd

            val connection = factory.newConnection()
            subject.onSuccess(connection.createChannel())
        }

        return subject
    }

    /**
     * Sends the event to the queue of async messages.
     */
    private fun sendEventToQueue(eventData: String, channel: Channel) {
        channel.queueDeclare(queueName, false, false, false, null)

        channel.basicPublish(
            "",
            queueName,
            null,
            eventData.toByteArray(StandardCharsets.UTF_8)
        )
    }
}