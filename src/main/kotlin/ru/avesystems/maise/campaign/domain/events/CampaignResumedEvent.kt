package ru.avesystems.maise.campaign.domain.events

import io.vertx.core.json.JsonObject
import java.time.LocalDateTime
import java.util.*

class CampaignResumedEvent(id: UUID, createdAt: LocalDateTime) : AbstractDomainEvent(id, createdAt) {
    override fun toJson(): JsonObject {
        TODO("Not yet implemented")
    }

    override val type: String
        get() = TODO("Not yet implemented")
}