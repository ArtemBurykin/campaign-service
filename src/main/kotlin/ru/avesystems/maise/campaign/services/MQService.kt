package ru.avesystems.maise.campaign.services

import io.vertx.codegen.annotations.ProxyGen
import io.vertx.codegen.annotations.VertxGen

/**
 * The service to encapsulate sending and getting async messages from a message broker.
 */
@VertxGen
@ProxyGen
interface MQService {
    /**
     * Sends the event data to a message broker queue.
     *
     * @param eventData the data of an event as a JSON string
     */
    fun sendEventDataToQueue(eventData: String)
}
