package ru.avesystems.maise.campaign

import io.reactivex.Completable
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.DeploymentOptions
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.config.ConfigRetriever
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.core.RxHelper
import io.vertx.reactivex.ext.web.Router
import io.vertx.reactivex.ext.web.RoutingContext
import io.vertx.reactivex.ext.web.handler.BodyHandler
import ru.avesystems.maise.campaign.domain.events.CampaignCreatedEvent
import ru.avesystems.maise.campaign.handlers.CreateCampaignHandler
import ru.avesystems.maise.campaign.handlers.GetAllCampaignsHandler
import ru.avesystems.maise.campaign.handlers.GetCampaignByIdHandler

/**
 * The main verticle to launch the app. The bootstrap is located here.
 * The logic is put to 3 verticles.
 * The read verticle is the read side of the module. The list data can be read from it.
 * The event store verticle to register all events occurred to a campaign. A campaign can be restored from the events.
 * The rabbitMQ verticle to send an event to start the mailing.
 */
class MainVerticle : AbstractVerticle() {

    /**
     * Starts the app.
     */
    override fun rxStart(): Completable {
        val envStore: ConfigStoreOptions = ConfigStoreOptions()
            .setType("env")

        val options: ConfigRetrieverOptions = ConfigRetrieverOptions().addStore(envStore)
        val retriever: ConfigRetriever = ConfigRetriever.create(vertx, options)

        return retriever.rxGetConfig().flatMapCompletable { ar ->
            deployVerticles(ar)
        }
    }

    private fun deployVerticles(config: JsonObject): Completable {
        val options = DeploymentOptions()
        options.config = config

        val readVrt = ReadVerticle()
        val readVrtDeployment = RxHelper.deployVerticle(vertx, readVrt, options)

        val eventStoreVrt = EventStoreVerticle()
        val verticlesDeployment = readVrtDeployment.mergeWith(RxHelper.deployVerticle(vertx, eventStoreVrt, options))

        registerObjectCodecs()

        val router = Router.router(vertx)

        registerRequestHandlers(router)

        router.errorHandler(500) { rc: RoutingContext ->
            System.err.println("Handling failure")
            val failure = rc.failure()
            failure?.printStackTrace()
        }

        return verticlesDeployment.flatMapCompletable {
            vertx.createHttpServer()
                .requestHandler(router::handle)
                .rxListen(8080).ignoreElement()
        }
    }

    /**
     * Registers object codecs to be allowed transfer POJO through the event bus.
     */
    private fun registerObjectCodecs() {
        vertx.eventBus().delegate.registerDefaultCodec(
            CampaignListItem::class.java, GenericCodec(CampaignListItem::class.java)
        )

        vertx.eventBus().delegate.registerDefaultCodec(
            CampaignCreatedEvent::class.java, GenericCodec(CampaignCreatedEvent::class.java)
        )
    }

    /**
     * Registers the handlers for different requests.
     */
    private fun registerRequestHandlers(router: Router) {
        router.get("/campaigns").handler(GetAllCampaignsHandler.getAllCampaignsClient(vertx))
        router.get("/campaigns/:id").handler(GetCampaignByIdHandler.getCampaignClient(vertx))

        router.post("/campaigns")
            .handler(BodyHandler.create())
            .handler(CreateCampaignHandler.createCampaignClient(vertx))
    }
}
