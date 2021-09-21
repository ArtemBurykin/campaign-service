package ru.avesystems.maise.campaign.domain.events

import io.vertx.core.json.JsonObject
import java.time.LocalDateTime
import java.util.*

class CampaignDeletedEvent(id: UUID, createdAt: LocalDateTime) : AbstractDomainEvent(id, createdAt) {
    override fun toJson(): JsonObject {
        return JsonObject()
    }

    override val type: String
        get() = TODO("Not yet implemented")
}
