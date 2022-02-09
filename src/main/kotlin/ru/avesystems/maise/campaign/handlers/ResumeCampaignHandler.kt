package ru.avesystems.maise.campaign.handlers

import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.RoutingContext
import ru.avesystems.maise.campaign.codecs.OptionalCampaign
import ru.avesystems.maise.campaign.domain.CampaignCannotBeResumedException
import ru.avesystems.maise.campaign.models.ErrorResponse

class ResumeCampaignHandler {
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

                    try {
                        campaign.resume()
                    } catch(e: CampaignCannotBeResumedException) {
                        val error = ErrorResponse("The campaign cannot be resumed")

                        context.response()
                            .putHeader("content-type", "application/json")
                            .setStatusCode(409).end(error.toJson().toString())

                        return@subscribe
                    }

                    // if there is no new events added, it means the campaign's already resumed
                    if (campaign.events.count() == 0) {
                        context.response()
                            .putHeader("content-type", "application/json")
                            .setStatusCode(204).end()

                        return@subscribe
                    }

                    val campaignResumedEvent = campaign.events.last()

                    eventBus.publish("campaigns.events.occur", campaignResumedEvent)

                    context.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(204).end()
                }
            }
        }
    }

}