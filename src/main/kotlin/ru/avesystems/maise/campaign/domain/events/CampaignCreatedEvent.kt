package ru.avesystems.maise.campaign.domain.events

import io.vertx.core.json.JsonObject
import ru.avesystems.maise.campaign.domain.Config
import java.time.LocalDateTime
import java.util.*

class CampaignCreatedEvent(
    id: UUID,
    val title: String,
    val templateTypeId: UUID,
    val templateTypeConfig: Map<String, Any>,
    val recipientLists: Map<UUID, Config>,
    createdAt: LocalDateTime
) : AbstractDomainEvent(id, createdAt) {

    companion object : EventCreator<CampaignCreatedEvent>() {
        override fun fromJson(obj: JsonObject, createdAt: LocalDateTime): CampaignCreatedEvent {
            val id = UUID.fromString(obj.getString("id"))
            val title = obj.getString("title")
            val templateTypeId = UUID.fromString(obj.getString("templateTypeId"))
            val templateConfig = obj.getJsonObject("typeConfig")
            val templateMap = templateConfig.map

            val recipientListData = obj.getJsonObject("recipientsList")

            val recipientList = mutableMapOf<UUID, Map<String, Any>>()
            recipientListData.forEach { (recipientId: String, configObj: Any) ->
                val configData = configObj as JsonObject

                val config = mutableMapOf<String, Any>()
                configData.forEach { (key, value) ->
                    config[key] = value
                }

                recipientList[UUID.fromString(recipientId)] = config
            }

            return CampaignCreatedEvent(
                id,
                title,
                templateTypeId,
                templateMap,
                recipientList,
                createdAt
            )
        }

        override fun getType(): String {
            return "CampaignCreatedEvent"
        }
    }

    override fun toJson(): JsonObject {
        val json = JsonObject()

        json.put("id", id.toString())
        json.put("title", title)
        json.put("templateTypeId", templateTypeId.toString())

        val typeConfig = JsonObject()
        templateTypeConfig.forEach { (key, value) ->
            typeConfig.put(key, value)
        }
        json.put("typeConfig", typeConfig)

        val recipientsListData = JsonObject()
        recipientLists.forEach { (recipientId, config) ->
            val configData = JsonObject()
            config.forEach { (key, value) -> configData.put(key, value) }
            recipientsListData.put(recipientId.toString(), configData)
        }
        json.put("recipientsList", recipientsListData)

        return json
    }

    override val type = getType()
}