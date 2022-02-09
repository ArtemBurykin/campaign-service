package ru.avesystems.maise.campaign.domain.events

import io.vertx.core.json.JsonObject
import java.time.LocalDateTime
import java.util.*

class CampaignResumedEvent(id: UUID, createdAt: LocalDateTime) : AbstractDomainEvent(id, createdAt) {
    override fun toJson(): JsonObject {
        val json = JsonObject()
        json.put("id", id.toString())

        return json
    }

    override val type = getType()

    companion object : EventCreator<CampaignResumedEvent>() {
        override fun fromJson(obj: JsonObject, createdAt: LocalDateTime): CampaignResumedEvent {
            val id = UUID.fromString(obj.getString("id"))

            return CampaignResumedEvent(id, createdAt)
        }

        override fun getType(): String {
            return "CampaignResumedEvent"
        }
    }
}