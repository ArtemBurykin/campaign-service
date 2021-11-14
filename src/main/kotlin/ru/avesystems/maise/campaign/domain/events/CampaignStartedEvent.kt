package ru.avesystems.maise.campaign.domain.events

import io.vertx.core.json.JsonObject
import java.time.LocalDateTime
import java.util.*

class CampaignStartedEvent(id: UUID, createdAt: LocalDateTime) : AbstractDomainEvent(id, createdAt) {
    override fun toJson(): JsonObject {
        val json = JsonObject()
        json.put("id", id.toString())

        return json
    }

    override val type = getType()

    companion object : EventCreator<CampaignStartedEvent>() {
        override fun fromJson(obj: JsonObject, createdAt: LocalDateTime): CampaignStartedEvent {
            val id = UUID.fromString(obj.getString("id"))

            return CampaignStartedEvent(id, createdAt)
        }

        override fun getType(): String {
            return "CampaignStartedEvent"
        }
    }
}