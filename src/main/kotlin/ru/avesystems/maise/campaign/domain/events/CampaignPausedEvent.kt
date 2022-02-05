package ru.avesystems.maise.campaign.domain.events

import io.vertx.core.json.JsonObject
import java.time.LocalDateTime
import java.util.*

class CampaignPausedEvent(id: UUID, createdAt: LocalDateTime) : AbstractDomainEvent(id, createdAt) {
    override fun toJson(): JsonObject {
        val json = JsonObject()
        json.put("id", id.toString())

        return json
    }

    override val type = getType()

    companion object : EventCreator<CampaignPausedEvent>() {
        override fun fromJson(obj: JsonObject, createdAt: LocalDateTime): CampaignPausedEvent {
            val id = UUID.fromString(obj.getString("id"))

            return CampaignPausedEvent(id, createdAt)
        }

        override fun getType(): String {
            return "CampaignPausedEvent"
        }
    }
}
