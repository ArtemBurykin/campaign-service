package ru.avesystems.maise.campaign.domain

import ru.avesystems.maise.campaign.domain.events.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * The mailing campaign. The aggregate to control the process of a mailing.
 */
class Campaign() {
    val events: MutableList<AbstractDomainEvent> = mutableListOf()
    var version: Long = 0

    /**
     * A map with ids of recipient lists (another aggregate) and a config for each of them.
     * The config may contain something like templates that can be substituted during a mailing.
     */
    lateinit var recipientLists: Map<UUID, Map<String, Any>>; private set

    /**
     * The config to configure a template type in order to make it more specific.
     * For example global variables, that are not recipient specific.
     */
    lateinit var templateTypeConfig: Map<String, Any>; private set

    /**
     * The id of the template (another aggregate) for the mail campaign.
     */
    lateinit var templateTypeId: UUID; private set

    /**
     * The title for the campaign.
     */
    lateinit var title: String; private set

    /**
     * The current state of the campaign. It's used to implement the state machine to control the state of a mailing.
     */
    var state = CampaignState.Initial; private set

    lateinit var id: UUID; private set

    constructor (cmd: CreateCampaign) : this() {
        val id = UUID.randomUUID()

        val event = CampaignCreatedEvent(
                id,
                cmd.title,
                cmd.templateTypeId,
                cmd.templateTypeConfig,
                cmd.recipientLists,
                LocalDateTime.now()
        )

        record(event)
    }

    /**
     * Starts the campaign. Launches the mailing (sending emails to users).
     */
    fun start() {
        if (state != CampaignState.Initial) {
            return
        }

        val event = CampaignStartedEvent(id, LocalDateTime.now())

        record(event)
    }

    /**
     * Pauses the campaign. Later it can be resumed from the position where it was paused.
     */
    fun pause() {
        if (state  == CampaignState.Initial) {
            throw CampaignNotStartedException("The campaign is not started")
        }

        val event = CampaignPausedEvent(id, LocalDateTime.now())

        record(event)
    }

    private fun on(e: CampaignCreatedEvent) {
        id = e.id
        title = e.title
        templateTypeId = e.templateTypeId
        templateTypeConfig = e.templateTypeConfig
        recipientLists = e.recipientLists
    }

    private fun on(e: CampaignStartedEvent) {
        state = CampaignState.Sending
    }

    private fun on(e: CampaignPausedEvent) {
        state = CampaignState.Paused
    }

    fun apply(e: AbstractDomainEvent) {
        when (e) {
            is CampaignCreatedEvent -> on(e)
            is CampaignPausedEvent -> on(e)
            is CampaignStartedEvent -> on(e)
            else -> throw UnknownEventTypeException()
        }

        val tz = ZoneOffset.of("+00:00")
        version = e.createdAt.toEpochSecond(tz)
    }

    private fun record(e: AbstractDomainEvent) {
        apply(e)
        events.add(e)
    }
}
