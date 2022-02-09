package ru.avesystems.maise.campaign.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.Test
import ru.avesystems.maise.campaign.domain.events.*
import java.util.*

class CampaignTest {

    @Test
    fun testCreate_Successful() {
        val title = "some title"
        val templateTypeId = UUID.randomUUID()
        val templateConfig = mapOf("item" to "value")
        val recipientList = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))

        val createCommand = CreateCampaign(
            title,
            templateTypeId,
            templateConfig,
            recipientList
        )

        val campaign = Campaign(createCommand)

        val campaignId = campaign.id

        assertTrue(campaign.events[0] is CampaignCreatedEvent)

        val event = campaign.events[0] as CampaignCreatedEvent
        assertEquals(title, event.title)
        assertEquals(campaignId, event.id)
        assertEquals(templateTypeId, event.templateTypeId)
        assertEquals(templateConfig, event.templateTypeConfig)
        assertEquals(recipientList, event.recipientLists)

        assertEquals(title, campaign.title)
        assertEquals(templateTypeId, campaign.templateTypeId)
        assertEquals(templateConfig, campaign.templateTypeConfig)
        assertEquals(recipientList, campaign.recipientLists)
        assertNotEquals(0, campaign.version)

        assertEquals(CampaignState.Initial, campaign.state)
    }

    @Test
    fun testStart_InitialState_ShouldChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Sending, campaign.state)

        assertTrue(campaign.events.size == 2)
        assertTrue(campaign.events[1] is CampaignStartedEvent)
        assertEquals(campaign.id, campaign.events[1].id)
    }

    @Test
    fun testStart_SendingState_ShouldThrowException() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        val exceptionThrown = assertThrows(CampaignAlreadyStartedException::class.java) {
            campaign.start()
        }
        assertEquals("The campaign is already started", exceptionThrown.message)

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Sending, campaign.state)
    }

    @Test
    fun testStart_Paused_ShouldThrowException() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.pause()
        val exceptionThrown = assertThrows(CampaignAlreadyStartedException::class.java) {
            campaign.start()
        }
        assertEquals("The campaign is already started", exceptionThrown.message)

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Paused, campaign.state)
    }

    @Test
    fun testPause_InitialState_ShouldThrowException() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        val exceptionThrown = assertThrows(CampaignNotStartedException::class.java) {
            campaign.pause()
        }

        assertEquals("The campaign is not started", exceptionThrown.message)

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Initial, campaign.state)
    }

    @Test
    fun testPause_SendingState_ShouldChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.pause()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Paused, campaign.state)

        assertTrue(campaign.events.size == 3)
        assertTrue(campaign.events[2] is CampaignPausedEvent)

        val event = campaign.events[2]

        assertEquals(campaign.id, event.id)
    }

    @Test
    fun testPause_Paused_ShouldNotChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.pause()
        val exceptionThrown = assertThrows(CampaignAlreadyPausedException::class.java) {
            campaign.pause()
        }
        assertEquals("The campaign already paused", exceptionThrown.message)

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Paused, campaign.state)
    }
}

