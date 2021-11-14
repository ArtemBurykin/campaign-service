package ru.avesystems.maise.campaign.handlers

import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.RoutingContext
import ru.avesystems.maise.campaign.codecs.OptionalCampaign
import ru.avesystems.maise.campaign.models.ErrorResponse

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
                    val campaignHolder = result.body() as OptionalCampaign
                    val campaign = campaignHolder.campaign

                    if (campaign == null) {
                        val error = ErrorResponse("The campaign is not found")

                        context.response()
                            .putHeader("content-type", "application/json")
                            .setStatusCode(404).end(error.toJson().toString())

                        return@subscribe
                    }

                    val campaignData = JsonObject()

                    campaignData.put("id", campaign.id.toString())
                    campaignData.put("title", campaign.title)
                    campaignData.put("templateId", campaign.templateTypeId.toString())

                    val configData = JsonObject()
                    campaign.templateTypeConfig.forEach { (key, value) ->
                        configData.put(key, value)
                    }

                    campaignData.put("templateConfig", configData)

                    val recipientsListData = JsonObject()
                    campaign.recipientLists.forEach { (recipientId, config) ->
                        val recipientConfData = JsonObject()
                        config.forEach { (key, value) ->
                            recipientConfData.put(key, value)
                        }

                        recipientsListData.put(recipientId.toString(), recipientConfData)
                    }

                    campaignData.put("recipients", recipientsListData)
                    campaignData.put("state", campaign.state)

                    context.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(200).end(campaignData.toString())
                }
            }
        }
    }
}