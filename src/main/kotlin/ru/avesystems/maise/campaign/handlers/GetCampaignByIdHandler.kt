package ru.avesystems.maise.campaign.handlers

import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.RoutingContext
import ru.avesystems.maise.campaign.models.CampaignItem

/**
 * The handler to get a campaign item from the DB.
 */
class GetCampaignByIdHandler {
    companion object {
        fun getCampaignClient(vertx: Vertx): (context: RoutingContext) -> Unit {
            return { context ->
                val id = context.pathParam("id")
                val eventBus = vertx.eventBus()

                eventBus.rxRequest<Any>("campaigns.restoreById", id).subscribe { result ->
                    val campaignReadModel = result.body() as CampaignItem
                    val campaignData = JsonObject()

                    campaignData.put("id", campaignReadModel.id.toString())
                    campaignData.put("title", campaignReadModel.title)
                    campaignData.put("templateId", campaignReadModel.templateId.toString())

                    val configData = JsonObject()
                    campaignReadModel.templateConfig.forEach { (key, value) ->
                        configData.put(key, value)
                    }

                    campaignData.put("templateConfig", configData)

                    val recipientsListData = JsonObject()
                    campaignReadModel.recipients.forEach { (recipientId, config) ->
                        val recipientConfData = JsonObject()
                        config.forEach { (key, value) ->
                            recipientConfData.put(key, value)
                        }

                        recipientsListData.put(recipientId.toString(), recipientConfData)
                    }

                    campaignData.put("recipients", recipientsListData)
                    campaignData.put("state", campaignReadModel.state)

                    context.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(200).end(campaignData.toString())
                }
            }
        }
    }
}