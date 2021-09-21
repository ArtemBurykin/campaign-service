package ru.avesystems.maise.campaign.domain.events

import io.vertx.core.json.JsonObject
import java.time.LocalDateTime
import java.util.*

/**
 * The base for all domain events in the service.
 */
abstract class AbstractDomainEvent(val id: UUID, val createdAt: LocalDateTime) {
    abstract fun toJson(): JsonObject
    abstract val type: String
}
