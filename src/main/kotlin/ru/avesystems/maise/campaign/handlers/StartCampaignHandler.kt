package ru.avesystems.maise.campaign.handlers

import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.RoutingContext
import ru.avesystems.maise.campaign.codecs.OptionalCampaign
import ru.avesystems.maise.campaign.models.ErrorResponse

/**
 * The handler to start a campaign
 */
class StartCampaignHandler {
    companion object {
        fun getClient(vertx: Vertx): (context: RoutingContext) -> Unit {
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
                            .setStatusCode(422).end(error.toJson().toString())

                        return@subscribe
                    }

                    campaign.start()

                    // if there is no new events added, it means the campaign's already started
                    if (campaign.events.count() == 0) {
                        context.response()
                            .putHeader("content-type", "application/json")
                            .setStatusCode(204).end()

                        return@subscribe
                    }

                    val campaignStartedEvent = campaign.events.last()

                    eventBus.publish("campaigns.events.occur", campaignStartedEvent)

                    context.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(204).end()
                }
            }
        }
    }
}