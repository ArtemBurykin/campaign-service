package ru.avesystems.maise.campaign.handlers

import io.vertx.core.json.JsonObject
import io.vertx.reactivex.ext.web.RoutingContext
import ru.avesystems.maise.campaign.domain.Campaign
import ru.avesystems.maise.campaign.domain.CreateCampaign
import java.util.*
import io.vertx.reactivex.core.Vertx

/**
 * The request handler to create a campaign.
 */
class CreateCampaignHandler {
    companion object {
        fun createCampaignClient(vertx: Vertx) : (context: RoutingContext) -> Unit {
            return { context: RoutingContext ->
                val campaignData = context.bodyAsJson
                val templateId = UUID.fromString(campaignData.getString("templateId"))

                val templateConfig = mutableMapOf<String, Any>()
                campaignData.getJsonObject("templateConfig").forEach {  (key: String, value: Any) ->
                    templateConfig[key] = value
                }

                val recipients = mutableMapOf<UUID, Map<String, Any>>()

                campaignData.getJsonObject("recipients").forEach { (recipient: String, config: Any) ->
                    val recipientUUID = UUID.fromString(recipient)
                    val configMap = mutableMapOf<String, Any>()

                    JsonObject.mapFrom(config).forEach { (key: String, value: Any) ->
                        configMap[key] = value
                    }

                    recipients[recipientUUID] = configMap
                }

                val createCampaignCommand = CreateCampaign(
                    campaignData.getString("title"),
                    templateId,
                    templateConfig,
                    recipients
                )

                val campaign = Campaign(createCampaignCommand)
                val campaignCreatedEvent = campaign.events[0]

                val eventBus = vertx.eventBus()
                eventBus.publish("campaigns.events.occur", campaignCreatedEvent)

                val response = JsonObject(mapOf("id" to campaign.id))

                context.response()
                    .putHeader("content-type", "application/json")
                    .setStatusCode(201).end(response.encode())
            }
        }
    }
}
