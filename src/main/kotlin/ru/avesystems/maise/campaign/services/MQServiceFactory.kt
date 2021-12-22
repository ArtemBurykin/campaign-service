package ru.avesystems.maise.campaign.services

import io.vertx.core.Vertx

object MQServiceFactory {
    fun create(vertx: Vertx): MQService = RabbitMQService(vertx)

    fun createProxy(vertx: Vertx, address: String): MQService {
        return MQServiceVertxEBProxy(vertx, address)
    }
}