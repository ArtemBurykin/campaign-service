package ru.avesystems.maise.campaign.models

import io.vertx.core.json.JsonObject

/**
 * The class to cast an error response for an http request.
 */
data class ErrorResponse(val error: String) {
    fun toJson(): JsonObject {
        val jsonError = JsonObject()
        jsonError.put("error", error)

        return jsonError
    }
}
