package ru.avesystems.maise.campaign.domain.events

import io.vertx.core.json.JsonObject
import java.time.LocalDateTime
import java.util.*

class CampaignStartedEvent(id: UUID, createdAt: LocalDateTime) : AbstractDomainEvent(id, createdAt) {
    override fun toJson(): JsonObject {
        TODO("Not yet implemented")
    }

    override val type: String
        get() = TODO("Not yet implemented")

    companion object : EventCreator<CampaignStartedEvent>() {
        override fun fromJson(obj: JsonObject, createdAt: LocalDateTime): CampaignStartedEvent {
            val id = UUID.fromString(obj.getString("id"))

            return CampaignStartedEvent(id, createdAt)
        }

        override fun getType(): String {
            TODO("Not yet implemented")
        }
    }
}