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
import io.reactivex.subjects.AsyncSubject
import org.skyscreamer.jsonassert.JSONAssert

class CampaignControllerTests {

    @Test
    fun createAndGetCampaignFromList() {
        val baseUrl = "http://0.0.0.0:90"

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
            post("$baseUrl/campaigns")
        } Then {
            statusCode(201)
        }

        val createdId = response.extract().body().jsonPath().getString("id")

        val collectionUrl = "http://0.0.0.0:90/campaigns"

        val getResponse = Given {
            request()
        } When {
            get(collectionUrl)
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
        val baseUrl = "http://0.0.0.0:90"

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
            post("$baseUrl/campaigns")
        } Then {
            statusCode(201)
        }

        val createdId = response.extract().body().jsonPath().getString("id")

        val itemUrl = "http://0.0.0.0:90/campaigns/$createdId"

        val getResponse = Given {
            request()
        } When {
            get(itemUrl)
        } Then {
            statusCode(200)
        }

        val campaignsResponse = getResponse.extract().jsonPath()

        val foundCampaignTitle = campaignsResponse.getString("title")
        assertEquals(campaignTitle, foundCampaignTitle)

        val foundTemplateTypeId = campaignsResponse.getString("templateId")
        assertEquals(templateTypeId, foundTemplateTypeId)

        val foundConfig = campaignsResponse.getJsonObject<Map<String, Any>>("templateConfig")
        val expectedConfig = mapOf("prop" to "Text")
        assertEquals(expectedConfig, foundConfig)

        val foundRecipientList = campaignsResponse.getJsonObject<Map<String, Map<String, Any>>>("recipients")
        val expectedRecipientList = mapOf(recipientId to mapOf("option" to "test"))
        assertEquals(expectedRecipientList, foundRecipientList)

        val state = campaignsResponse.get<String>("state")
        assertEquals(CampaignState.Initial.toString(), state)
    }

    @Test
    fun createAndCheckThatEventSent() {
        val baseUrl = "http://0.0.0.0:90"

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

        val factory = ConnectionFactory()
        factory.port = 5670
        val connection = factory.newConnection()
        val channel = connection.createChannel()

        val response = Given {
            request().header("ContentType", "application/json").and().body(createJson)
        } When {
            post("$baseUrl/campaigns")
        } Then {
            statusCode(201)
        }

        val messageSbj = AsyncSubject.create<String>()

        val queueName = "campaign_msgs"

        channel.queueDeclare(queueName, false, false, false, null)

        val deliverCallback = DeliverCallback { _: String?, delivery: Delivery ->
            messageSbj.onNext(String(delivery.body, StandardCharsets.UTF_8))
            messageSbj.onComplete()
        }

        val cancelCallback = CancelCallback { }

        channel.basicConsume(queueName, true, deliverCallback, cancelCallback)

        val createdId = response.extract().body().jsonPath().getString("id")

        val event = JSONObject()
        event.put("id", createdId)
        event.put("type", "CampaignCreatedEvent")
        event.put("title", campaignTitle)
        event.put("templateId", templateTypeId)
        event.put("templateConfig", templateConfig)
        event.put("recipients", recipientList)

        val messageObj = JSONObject(messageSbj.value)
        JSONAssert.assertEquals(event, messageObj, false)
    }
}
