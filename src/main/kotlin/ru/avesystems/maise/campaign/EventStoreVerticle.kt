package ru.avesystems.maise.campaign

import io.reactivex.Completable
import io.vertx.reactivex.core.AbstractVerticle
import ru.avesystems.maise.campaign.db.EventStoreCampaignRepository
import ru.avesystems.maise.campaign.domain.Campaign
import ru.avesystems.maise.campaign.domain.events.AbstractDomainEvent
import ru.avesystems.maise.campaign.model.CampaignItem
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

                val campaignData = CampaignItem(
                    campaign.id,
                    campaign.title,
                    campaign.templateTypeId,
                    campaign.templateTypeConfig,
                    campaign.state,
                    campaign.recipientLists
                )

                message.reply(campaignData)
            }
        }

        return Completable.complete()
    }
}
