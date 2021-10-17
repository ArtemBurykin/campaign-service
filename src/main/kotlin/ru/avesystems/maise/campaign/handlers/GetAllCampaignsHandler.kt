package ru.avesystems.maise.campaign.handlers

import io.vertx.core.json.Json
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.RoutingContext
import ru.avesystems.maise.campaign.codec.CampaignItemsHolder

/**
 * The request handler to get all campaigns from the DB.
 */
class GetAllCampaignsHandler {
    companion object {
        fun getAllCampaignsClient(vertx: Vertx): (context: RoutingContext) -> Unit {
            return { context ->
                val eventBus = vertx.eventBus()

                eventBus.rxRequest<Any>("campaigns.read.getAll", null).subscribe { result ->
                    val campaignsHolder = result.body() as CampaignItemsHolder
                    val campaigns = campaignsHolder.data
                    val encodedCampaignsList = Json.encodePrettily(campaigns)

                    context.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(200).end(encodedCampaignsList)
                }
            }
        }
    }
}
