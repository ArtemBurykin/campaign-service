package ru.avesystems.maise.campaign.repositories

import io.vertx.pgclient.PgConnectOptions
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.pgclient.PgPool
import io.vertx.sqlclient.PoolOptions

/**
 * The service to provide a connection to the DB.
 */
class PgConnection(
    private val vertx: Vertx,
    private val dbName: String,
    private val dbUser: String,
    private val dbPwd: String,
    private val dbHost: String
) {
    fun connectToDB(): PgPool {
        val connectOptions = PgConnectOptions()
            .setHost(dbHost)
            .setDatabase(dbName)
            .setUser(dbUser)
            .setPassword(dbPwd)

        val poolOptions = PoolOptions()
            .setMaxSize(10)

        return PgPool.pool(vertx, connectOptions, poolOptions)
    }
}