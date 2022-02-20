package ru.avesystems.maise.campaign.domain.events

import io.vertx.core.json.JsonObject
import java.time.LocalDateTime
import java.util.*

class CampaignDeletedEvent(id: UUID, createdAt: LocalDateTime) : AbstractDomainEvent(id, createdAt) {
    override fun toJson(): JsonObject {
        val json = JsonObject()
        json.put("id", id.toString())

        return json
    }

    override val type = getType()

    companion object : EventCreator<CampaignDeletedEvent>() {
        override fun fromJson(obj: JsonObject, createdAt: LocalDateTime): CampaignDeletedEvent {
            val id = UUID.fromString(obj.getString("id"))

            return CampaignDeletedEvent(id, createdAt)
        }

        override fun getType(): String {
            return "CampaignDeletedEvent"
        }
    }
}