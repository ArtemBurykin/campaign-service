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
    fun testStart_SendingState_ShouldNotChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.start()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Sending, campaign.state)
    }

    @Test
    fun testStart_Paused_ShouldNotChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.pause()
        campaign.start()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Paused, campaign.state)
    }

    @Test
    fun testStart_Stopped_ShouldChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.stop()
        campaign.start()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Sending, campaign.state)

        assertTrue(campaign.events.size == 4)

        assertTrue(campaign.events[3] is CampaignStartedEvent)

        val event = campaign.events[3]
        assertEquals(campaign.id, event.id)
    }

    @Test
    fun testStop_SendingState_ShouldStop() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.stop()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Stopped, campaign.state)

        assertTrue(campaign.events.size == 3)
        assertTrue(campaign.events[2] is CampaignStoppedEvent)

        val event = campaign.events[2]
        assertEquals(campaign.id, event.id)
    }

    @Test
    fun testStop_InitialState_ShouldNotChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.stop()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Initial, campaign.state)
    }

    @Test
    fun testStop_PausedState_ShouldStop() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.pause()
        campaign.stop()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Stopped, campaign.state)

        assertTrue(campaign.events.size == 4)
        assertTrue(campaign.events[3] is CampaignStoppedEvent)

        val event = campaign.events[3]
        assertEquals(campaign.id, event.id)
    }

    @Test
    fun testStop_Stopped_ShouldNotChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.stop()
        campaign.stop()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Stopped, campaign.state)
    }

    @Test
    fun testPause_InitialState_ShouldNotChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.pause()

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
    fun testPause_StoppedState_ShouldNotChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.stop()
        campaign.pause()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Stopped, campaign.state)
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
        campaign.pause()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Paused, campaign.state)
    }

    @Test
    fun testResume_InitialState_ShouldNotChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.resume()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Initial, campaign.state)
    }

    @Test
    fun testResume_SendingState_ShouldNotChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.resume()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Sending, campaign.state)
    }

    @Test
    fun testResume_Stopped_ShouldNotChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.stop()
        campaign.resume()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Stopped, campaign.state)
    }

    @Test
    fun testResume_Paused_ShouldChangeState() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.pause()
        campaign.resume()

        assertNotEquals(0, campaign.version)
        assertEquals(CampaignState.Sending, campaign.state)

        assertTrue(campaign.events.size == 4)
        assertTrue(campaign.events[3] is CampaignResumedEvent)

        val event = campaign.events[3]

        assertEquals(campaign.id, event.id)
    }

    @Test
    fun testDelete_InitialState_ShouldMarkAsDeleted() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.delete()

        assertNotEquals(0, campaign.version)
        assertTrue(campaign.deleted)

        assertTrue(campaign.events.size == 2)
        assertTrue(campaign.events[1] is CampaignDeletedEvent)

        val event = campaign.events[1]
        assertEquals(campaign.id, event.id)
    }

    @Test
    fun testDelete_SendingState_ShouldNotMarkAsDeleted() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.delete()

        assertNotEquals(0, campaign.version)
        assertFalse(campaign.deleted)
    }

    @Test
    fun testDelete_PausedState_ShouldNotMarkAsDeleted() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.pause()
        campaign.delete()

        assertNotEquals(0, campaign.version)
        assertFalse(campaign.deleted)
    }

    @Test
    fun testDelete_StoppedState_ShouldNotMarkAsDeleted() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.start()
        campaign.stop()
        campaign.delete()

        assertNotEquals(0, campaign.version)
        assertFalse(campaign.deleted)
    }

    @Test
    fun testStart_CampaignDeleted_ShouldThrowException() {
        val createCommand = CreateCampaign(
            title = "some title",
            templateTypeId = UUID.randomUUID(),
            templateTypeConfig = mapOf("item" to "value"),
            recipientLists = mapOf(UUID.randomUUID() to mapOf<String, Any>("test" to "any"))
        )

        val campaign = Campaign(createCommand)

        campaign.delete()

        val exceptionThrown = assertThrows(CampaignDeletedException::class.java) {
            campaign.start()
        }

        assertEquals("The campaign is deleted", exceptionThrown.message)
    }
}

