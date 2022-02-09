package ru.avesystems.maise.campaign.handlers

import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.ext.web.RoutingContext
import ru.avesystems.maise.campaign.codecs.OptionalCampaign
import ru.avesystems.maise.campaign.domain.CampaignAlreadyPausedException
import ru.avesystems.maise.campaign.domain.CampaignNotStartedException
import ru.avesystems.maise.campaign.models.ErrorResponse

class PauseCampaignHandler {
    companion object {
        fun getClient(vertx: Vertx): (context: RoutingContext) -> Unit {
            return { context ->
                val id = context.pathParam("id")
                val eventBus = vertx.eventBus()

                eventBus.rxRequest<Any>("campaigns.restoreById", id).subscribe { result ->
                    val campaignHolder = result.body() as OptionalCampaign
                    val campaign = campaignHolder.campaign

                    if (campaign == null) {
                        responseWithError(context, "The campaign is not found", 422)
                        return@subscribe
                    }

                    try {
                        campaign.pause()
                    } catch (e: CampaignNotStartedException) {
                        responseWithError(context, "The campaign is not started", 409)
                        return@subscribe
                    } catch (e: CampaignAlreadyPausedException) {
                        responseWithError(context, "The campaign already paused", 409)
                        return@subscribe
                    }

                    val campaignPausedEvent = campaign.events.last()

                    eventBus.publish("campaigns.events.occur", campaignPausedEvent)

                    context.response()
                        .putHeader("content-type", "application/json")
                        .setStatusCode(204).end()
                }
            }
        }

        /**
         * Creates a response with the error
         */
        private fun responseWithError(context: RoutingContext, errMessage: String, code: Int) {
            val error = ErrorResponse(errMessage)

            context.response()
                .putHeader("content-type", "application/json")
                .setStatusCode(code).end(error.toJson().toString())
        }
    }

}