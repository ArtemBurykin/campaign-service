package ru.avesystems.maise.campaign

import io.reactivex.Completable
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.AbstractVerticle
import ru.avesystems.maise.campaign.db.EventStoreCampaignRepository
import ru.avesystems.maise.campaign.domain.Campaign
import ru.avesystems.maise.campaign.domain.events.AbstractDomainEvent
import java.util.*

/**
 * The verticle to write data to the event store
 */
class EventStoreVerticle: AbstractVerticle() {
    private lateinit var eventStoreCampaignRepository: EventStoreCampaignRepository

    override fun rxStart(): Completable {
        val eventBus = vertx.eventBus()

        val dbName = config().getString("POSTGRES_DB")
        val dbUser = config().getString("POSTGRES_USER")
        val dbPwd = config().getString("POSTGRES_PASSWORD")
        val dbHost = config().getString("POSTGRES_HOST")
        eventStoreCampaignRepository = EventStoreCampaignRepository(vertx, dbName, dbUser, dbPwd, dbHost)

        eventBus.consumer<Any>("campaigns.events.occur") { message ->
            val event = message.body()

            if (event is AbstractDomainEvent) {
               eventStoreCampaignRepository.writeEvent(event).subscribe()
            }
        }

        eventBus.consumer<Any>("campaigns.restoreById") { message ->
            val id = message.body().toString()
            val uuid = UUID.fromString(id)

            eventStoreCampaignRepository.findAllEvents(uuid).subscribe { events ->
                val campaign = Campaign()
                events.forEach {
                    campaign.apply(it)
                }

                // IMHERE
                // TODO: move deserialization to the get item handler
                val campaignData = JsonObject()
                campaignData.put("id", campaign.id.toString())
                campaignData.put("title", campaign.title)
                campaignData.put("templateId", campaign.templateTypeId.toString())

                val configData = JsonObject()
                campaign.templateTypeConfig.forEach { (key, value) ->
                    configData.put(key, value)
                }

                campaignData.put("templateConfig", campaign.templateTypeConfig)

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

                message.reply(campaignData)
            }
        }

        return Completable.complete()
    }
}
