package ru.avesystems.maise.campaign.verticles

import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.eventbus.Message
import ru.avesystems.maise.campaign.repositories.EventStoreCampaignRepository
import ru.avesystems.maise.campaign.domain.Campaign
import ru.avesystems.maise.campaign.domain.events.AbstractDomainEvent
import ru.avesystems.maise.campaign.models.CampaignItem
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

        eventBus.consumer<Any>("campaigns.events.occur") { writeEventMessage(it) }

        eventBus.consumer<Any>("campaigns.restoreById") { message ->
            val id = message.body().toString()
            val uuid = UUID.fromString(id)

            restoreCampaignById(uuid).subscribe { campaign ->
                message.reply(campaign)
            }
        }

        return Completable.complete()
    }

    /**
     * Writes data from the message to the event store.
     */
    private fun writeEventMessage(message: Message<Any>) {
        val event = message.body()

        if (event is AbstractDomainEvent) {
            eventStoreCampaignRepository.writeEvent(event).subscribe()
        }
    }

    /**
     * Restores a campaign from the events received from the event store, and creates a data object with its data.
     */
    private fun restoreCampaignById(id: UUID): Single<CampaignItem> {
        return eventStoreCampaignRepository.findAllEvents(id).flatMap { events ->
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

            val campaignDataSingle = Single.just(campaignData)
            campaignDataSingle
        }
    }
}
