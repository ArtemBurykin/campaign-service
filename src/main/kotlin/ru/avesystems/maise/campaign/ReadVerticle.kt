package ru.avesystems.maise.campaign

import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.core.json.Json
import io.vertx.reactivex.core.AbstractVerticle
import ru.avesystems.maise.campaign.db.CampaignReadRepository
import ru.avesystems.maise.campaign.domain.events.CampaignCreatedEvent
import java.util.*

/**
 * The verticle to read or to write the read side of the system
 */
class ReadVerticle : AbstractVerticle() {
    private lateinit var readRepository: CampaignReadRepository

    override fun rxStart(): Completable {
        val eventBus = vertx.eventBus()

        val dbName = config().getString("POSTGRES_DB")
        val dbUser = config().getString("POSTGRES_USER")
        val dbPwd = config().getString("POSTGRES_PASSWORD")
        val dbHost = config().getString("POSTGRES_HOST")
        readRepository = CampaignReadRepository(vertx, dbName, dbUser, dbPwd, dbHost)

        eventBus.consumer<Any>("campaigns.read.getAll") { message ->
            getAllCampaigns().subscribe { campaigns ->
               message.reply(Json.encodePrettily(campaigns))
            }
        }

        eventBus.consumer<Any>("campaigns.events.occur") { message ->
            val event = message.body()

            if (event is CampaignCreatedEvent) {
                createCampaign(event.id, event.title).subscribe {
                    message.reply("")
                }
            }
        }

        return Completable.complete()
    }

    /**
     * Retrieves all campaigns from the DB.
     */
    private fun getAllCampaigns(): Single<List<CampaignListItem>> {
        return readRepository.retrieveList()
    }

    /**
     * Creates a new campaign for the list view.
     */
    private fun createCampaign(id: UUID, title: String): Completable {
        val campaign = CampaignListItem(id, title)

        return readRepository.create(campaign)
    }
}
