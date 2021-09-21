package ru.avesystems.maise.campaign.domain.events

import io.vertx.core.json.JsonObject
import java.time.LocalDateTime

/**
 * The class to inherit by a companion object in order to create a factory method to create events.
 */
abstract class EventCreator<T> {
    abstract fun fromJson(obj: JsonObject, createdAt: LocalDateTime): T
    abstract fun getType(): String
}
