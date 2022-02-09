package ru.avesystems.maise.campaign.repositories

import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.pgclient.PgConnectOptions
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.pgclient.PgPool
import io.vertx.reactivex.sqlclient.Tuple
import io.vertx.sqlclient.PoolOptions
import ru.avesystems.maise.campaign.models.CampaignListItem

/**
 * The abstraction to reduce the code connected with the DB. The low level interface to operate with
 * the campaign_list table.
 */
class CampaignReadRepository(
    private val client: PgPool
) {
    /**
     * Retrieves the list of campaigns.
     */
    fun retrieveList(): Single<List<CampaignListItem>> {
        val result = client.query("SELECT * FROM campaign_list").rxExecute()

        return result.flatMap { rows ->
            Single.just(rows.map { row ->
                CampaignListItem(row.getUUID("id"), row.getString("title"))
            })
        }
    }

    /**
     * Creates a representation of the campaign in the table.
     */
    fun create(campaignData: CampaignListItem): Completable {
        val id = campaignData.id
        val title = campaignData.title

        return client.preparedQuery("INSERT INTO campaign_list (id, title) VALUES ($1, $2)")
            .rxExecute(Tuple.of(id, title)).flatMapCompletable {
                Completable.never()
            }
    }
}
