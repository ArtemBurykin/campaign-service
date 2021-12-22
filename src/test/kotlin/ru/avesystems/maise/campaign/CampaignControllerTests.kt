package ru.avesystems.maise.campaign

import com.rabbitmq.client.CancelCallback
import com.rabbitmq.client.ConnectionFactory
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import ru.avesystems.maise.campaign.domain.CampaignState
import java.nio.charset.StandardCharsets
import java.util.*
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.BeforeClass
import org.skyscreamer.jsonassert.JSONAssert

class CampaignControllerTests {
    private val resourceUrl = "http://0.0.0.0:90/campaigns"

    companion object {
        private val messages = mutableListOf<String>()
        private const val queueName = "campaign_msgs"

        @BeforeClass @JvmStatic
        fun setup() {
            val factory = ConnectionFactory()
            factory.port = 5670
            val connection = factory.newConnection()
            val channel = connection.createChannel()
            channel.queueDeclare(queueName, false, false, false, null)

            val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
                messages.add(String(delivery.body, StandardCharsets.UTF_8))
            }

            val cancelCallback = CancelCallback { }

            channel.basicConsume(queueName, true, deliverCallback, cancelCallback)
        }
    }

    @After
    fun teardown() {
        val factory = ConnectionFactory()
        factory.port = 5670

        val connection = factory.newConnection()
        val channel = connection.createChannel()
        channel.queuePurge(queueName)
        messages.clear()
    }

    @Test
    fun createAndGetCampaignFromList() {
        val templateTypeId = UUID.randomUUID().toString()

        val typeConfig = JSONObject()
        typeConfig.put("text", "Text")

        val recipientTemplateConfig = JSONObject()
        recipientTemplateConfig.put("option", "test")

        val recipientList = JSONObject()
        recipientList.put(templateTypeId, recipientTemplateConfig)

        val jsonBody = JSONObject()
        val campaignTitle = "Test"
        jsonBody.put("title", campaignTitle)
        jsonBody.put("templateId", templateTypeId)
        jsonBody.put("templateConfig", typeConfig)
        jsonBody.put("recipients", recipientList)

        val createJson = jsonBody.toString(2)

        val response = Given {
            request().header("ContentType", "application/json").and().body(createJson)
        } When {
            post(resourceUrl)
        } Then {
            statusCode(201)
        }

        val createdId = response.extract().body().jsonPath().getString("id")

        val getResponse = Given {
            request()
        } When {
            get(resourceUrl)
        } Then {
            statusCode(200)
        }

        val campaignsResponse = getResponse.extract().jsonPath()

        val campaignIds = campaignsResponse.getList<String>("id")
        assertTrue(campaignIds.contains(createdId))

        val campaignTitles = campaignsResponse.getList<String>("title")
        assertEquals(campaignTitle, campaignTitles[0])
    }

    @Test
    fun createAndGetCampaignItem() {
        val templateTypeId = UUID.randomUUID().toString()

        val templateConfig = JSONObject()
        templateConfig.put("prop", "Text")

        val recipientTemplateConfig = JSONObject()
        recipientTemplateConfig.put("option", "test")

        val recipientList = JSONObject()
        val recipientId = UUID.randomUUID().toString()
        recipientList.put(recipientId, recipientTemplateConfig)

        val jsonBody = JSONObject()
        val campaignTitle = "Test"
        jsonBody.put("title", campaignTitle)
        jsonBody.put("templateId", templateTypeId)
        jsonBody.put("templateConfig", templateConfig)
        jsonBody.put("recipients", recipientList)

        val createJson = jsonBody.toString(2)

        val response = Given {
            request().header("ContentType", "application/json").and().body(createJson)
        } When {
            post(resourceUrl)
        } Then {
            statusCode(201)
        }

        val createdId = response.extract().body().jsonPath().getString("id")

        val itemUrl = "$resourceUrl/$createdId"

        val getResponse = Given {
            request()
        } When {
            get(itemUrl)
        } Then {
            statusCode(200)
        }

        val campaignResponse = getResponse.extract().jsonPath()

        val foundCampaignTitle = campaignResponse.getString("title")
        assertEquals(campaignTitle, foundCampaignTitle)

        val foundTemplateTypeId = campaignResponse.getString("templateId")
        assertEquals(templateTypeId, foundTemplateTypeId)

        val foundConfig = campaignResponse.getJsonObject<Map<String, Any>>("templateConfig")
        val expectedConfig = mapOf("prop" to "Text")
        assertEquals(expectedConfig, foundConfig)

        val foundRecipientList = campaignResponse.getJsonObject<Map<String, Map<String, Any>>>("recipients")
        val expectedRecipientList = mapOf(recipientId to mapOf("option" to "test"))
        assertEquals(expectedRecipientList, foundRecipientList)

        val state = campaignResponse.get<String>("state")
        assertEquals(CampaignState.Initial.toString(), state)
    }

    @Test
    fun createAndCheckThatEventSent() {
        val templateTypeId = UUID.randomUUID().toString()

        val templateConfig = JSONObject()
        templateConfig.put("prop", "Text")

        val recipientTemplateConfig = JSONObject()
        recipientTemplateConfig.put("option", "test")

        val recipientList = JSONObject()
        val recipientId = UUID.randomUUID().toString()
        recipientList.put(recipientId, recipientTemplateConfig)

        val jsonBody = JSONObject()
        val campaignTitle = "Test"
        jsonBody.put("title", campaignTitle)
        jsonBody.put("templateId", templateTypeId)
        jsonBody.put("templateConfig", templateConfig)
        jsonBody.put("recipients", recipientList)

        val createJson = jsonBody.toString()

        val response = Given {
            request().header("ContentType", "application/json").and().body(createJson)
        } When {
            post(resourceUrl)
        } Then {
            statusCode(201)
        }

        val createdId = response.extract().body().jsonPath().getString("id")

        val event = JSONObject()
        event.put("id", createdId)
        event.put("type", "CampaignCreatedEvent")
        event.put("title", campaignTitle)
        event.put("templateId", templateTypeId)
        event.put("templateConfig", templateConfig)
        event.put("recipients", recipientList)

        val messageObj = JSONObject(messages.last())
        JSONAssert.assertEquals(event, messageObj, false)
    }

    @Test
    fun createAndStartCampaign() {
        val createdId = createBasicCampaign()

        Given {
            request().header("ContentType", "application/json")
        } When {
            put("$resourceUrl/$createdId/start")
        } Then {
            statusCode(204)
        }

        val itemUrl = "$resourceUrl/$createdId"

        val getResponse = Given {
            request()
        } When {
            get(itemUrl)
        } Then {
            statusCode(200)
        }

        val campaignResponse = getResponse.extract().jsonPath()

        val state = campaignResponse.get<String>("state")
        assertEquals(CampaignState.Sending.toString(), state)
    }

    @Test
    fun startingCampaignTwice() {
        val createdId = createBasicCampaign()

        Given {
            request().header("ContentType", "application/json")
        } When {
            put("$resourceUrl/$createdId/start")
        } Then {
            statusCode(204)
        }

        val itemUrl = "$resourceUrl/$createdId"

        val itemResponse1 = Given {
            request()
        } When {
            get(itemUrl)
        } Then {
            statusCode(200)
        }

        val request1Data = itemResponse1.extract().jsonPath()

        val state1 = request1Data.get<String>("state")
        assertEquals(CampaignState.Sending.toString(), state1)

        // Make the second request in order to try request's idempotency
        Given {
            request().header("ContentType", "application/json")
        } When {
            put("$resourceUrl/$createdId/start")
        } Then {
            statusCode(204)
        }

        val itemResponse2 = Given {
            request()
        } When {
            get(itemUrl)
        } Then {
            statusCode(200)
        }

        val response2Data = itemResponse2.extract().jsonPath()

        val state2 = response2Data.get<String>("state")
        assertEquals(CampaignState.Sending.toString(), state2)
    }

    @Test
    fun startingNonExistentCampaign() {
        val nonExistentId = UUID.randomUUID().toString()

        Given {
            request().header("ContentType", "application/json")
        } When {
            put("$resourceUrl/$nonExistentId/start")
        } Then {
            statusCode(422)
            body("error", equalTo("The campaign is not found"))
        }
    }

    @Test
    fun eventThatCampaignStartedShouldBePublished() {
        val campaignId = createBasicCampaign()

        Given {
            request().header("ContentType", "application/json")
        } When {
            put("$resourceUrl/$campaignId/start")
        } Then {
            statusCode(204)
        }

        Thread.sleep(100)

        val event = JSONObject()
        event.put("id", campaignId)
        event.put("type", "CampaignStartedEvent")

        val messageObj = JSONObject(messages.last())
        JSONAssert.assertEquals(event, messageObj, false)
    }

    @Test
    fun getNonExistentCampaignItem() {
        val nonExistentId = UUID.randomUUID().toString()

        val itemUrl = "$resourceUrl/$nonExistentId"

        Given {
            request()
        } When {
            get(itemUrl)
        } Then {
            statusCode(404)
            body("error", equalTo("The campaign is not found"))
        }
    }

    /**
     * Creates a campaign and returns the id of the campaign.
     */
    private fun createBasicCampaign(): String {
        val templateTypeId = UUID.randomUUID().toString()
        val templateConfig = JSONObject()
        val recipientList = JSONObject()

        val jsonBody = JSONObject()
        val campaignTitle = "Test"
        jsonBody.put("title", campaignTitle)
        jsonBody.put("templateId", templateTypeId)
        jsonBody.put("templateConfig", templateConfig)
        jsonBody.put("recipients", recipientList)

        val createJson = jsonBody.toString(2)

        val response = Given {
            request().header("ContentType", "application/json").and().body(createJson)
        } When {
            post(resourceUrl)
        } Then {
            statusCode(201)
        }

        return response.extract().body().jsonPath().getString("id")
    }
}
