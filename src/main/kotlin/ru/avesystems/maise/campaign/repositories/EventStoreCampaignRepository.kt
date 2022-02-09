package ru.avesystems.maise.campaign.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.pgclient.PgPool
import io.vertx.reactivex.sqlclient.Tuple
import ru.avesystems.maise.campaign.domain.events.*
import java.util.*

/**
 * The repository to abstract the usage of the event store.
 */
class EventStoreCampaignRepository(
    private val client: PgPool
) {
    /**
     * Writes an event to the event store.
     */
    fun writeEvent(event: AbstractDomainEvent): Completable {
        val eventData = event.toJson().toString()
        val type = event.type
        val id = event.id

        return client
            .preparedQuery("INSERT INTO campaign(id, event_type, event_data, stamp) VALUES ($1, $2, $3, $4)")
            .rxExecute(Tuple.of(id, type, eventData, event.createdAt)).flatMapCompletable {
                Completable.never()
            }
    }

    /**
     * Retrieves all events for the entity with the id.
     */
    fun findAllEvents(id: UUID): Single<List<AbstractDomainEvent>> {
        val result = client.preparedQuery("SELECT * FROM campaign WHERE id=$1")
            .rxExecute(Tuple.of(id))

        return result.map { list ->
             list.map { row ->
                 val eventData = JsonObject(row.getString("event_data"))
                 val createdAt = row.getLocalDateTime("stamp")

                 val event = when (row.getString("event_type")) {
                     CampaignCreatedEvent.getType() -> {
                         CampaignCreatedEvent.fromJson(eventData, createdAt)
                     }
                     CampaignStartedEvent.getType() -> {
                         CampaignStartedEvent.fromJson(eventData, createdAt)
                     }
                     CampaignPausedEvent.getType() -> {
                         CampaignPausedEvent.fromJson(eventData, createdAt)
                     }
                     CampaignResumedEvent.getType() -> {
                         CampaignResumedEvent.fromJson(eventData, createdAt)
                     }
                     else -> {
                         throw Exception("Unknown type of the event")
                     }
                 }

                 event
            }
        }
    }
}
