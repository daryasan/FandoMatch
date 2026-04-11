package org.example.client

import io.github.oshai.kotlinlogging.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.UUID

@Service
class CoreAdapter(
    private val restTemplate: RestTemplate,
    @Value("\${clients.core-service.url}") private val coreUrl: String,
    @Value("\${service.api-key}") private val serviceApiKey: String
) {

    companion object : KLogging()

    fun matchExists(userId1: UUID, userId2: UUID): Boolean {
        return try {
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("X-Api-Key", serviceApiKey)
            }
            val body = mapOf("user_id_1" to userId1.toString(), "user_id_2" to userId2.toString())
            val response = restTemplate.exchange(
                "$coreUrl/core/match/internal/exists",
                HttpMethod.POST,
                HttpEntity(body, headers),
                Map::class.java
            )
            response.body?.get("exists") as? Boolean ?: false
        } catch (e: Exception) {
            logger.error { "Failed to check match between $userId1 and $userId2: ${e.message}" }
            false
        }
    }
}
