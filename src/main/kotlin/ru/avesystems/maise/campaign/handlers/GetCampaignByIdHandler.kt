package ru.avesystems.maise.campaign.handlers

import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.RoutingContext

class GetCampaignByIdHandler {
    companion object {
        fun getCampaignClient(vertx: Vertx): (context: RoutingContext) -> Unit {
            return { context ->
                val id = context.pathParam("id")
                val eventBus = vertx.eventBus()

                eventBus.rxRequest<Any>("campaigns.restoreById", id).subscribe { result ->
                    val campaignData = result.body() as JsonObject

                    context.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(200).end(campaignData.toString())
                }
            }
        }
    }
}