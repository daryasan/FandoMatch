package org.example.controller

import com.fandomatch.core.model.*
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.MatchesService
import org.example.service.TokenParserService
import org.example.util.getCurrentFiltersErrorResponse
import org.example.util.getMatchActionErrorResponse
import org.example.util.getMatchCandidateBatchErrorResponse
import org.example.util.getMatchFilterErrorResponse
import org.example.util.onControllerRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/core/match")
class MatchController(
    private val matchesService: MatchesService,
    private val tokenParserService: TokenParserService,
    @Value("\${service.api-key}") private val serviceApiKey: String,
) {

    companion object : KLogging()

    @PostMapping("/next")
    fun getNextBatch(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: MatchBatchRequest
    ): ResponseEntity<MatchCandidateBatchResponse> {

        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/match/next",
            metaUuid = uuid.toString(),
            errorMapper = { getMatchCandidateBatchErrorResponse(it) }
        ) {
            matchesService.getNextCandidates(userId = uuid, batchSize = request.batchSize)
        }
    }

    @PostMapping("/react")
    fun react(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: MatchActionRequest
    ): ResponseEntity<MatchActionResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/match/react",
            metaUuid = uuid.toString(),
            errorMapper = { getMatchActionErrorResponse(it) }
        ) {
            matchesService.react(
                userId = uuid,
                targetUuid = UUID.fromString(request.targetUuid),
                action = request.action.value
            )
        }
    }

    @PostMapping("/internal/exists")
    fun matchExists(
        @RequestHeader("X-Api-Key") apiKey: String,
        @RequestBody request: Map<String, String>
    ): ResponseEntity<Map<String, Boolean>> {
        if (apiKey != serviceApiKey) return ResponseEntity.status(403).build()
        val u1 = UUID.fromString(request["user_id_1"] ?: return ResponseEntity.badRequest().build())
        val u2 = UUID.fromString(request["user_id_2"] ?: return ResponseEntity.badRequest().build())
        return ResponseEntity.ok(mapOf("exists" to matchesService.areFriends(u1, u2)))
    }

    @GetMapping("/get_current_filters")
    fun getCurrentFilters(
        @RequestHeader("Authorization") token: String
    ): ResponseEntity<CurrentFiltersResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/match/get_current_filters",
            metaUuid = uuid.toString(),
            errorMapper = { getCurrentFiltersErrorResponse(it) }
        ) {
            matchesService.getCurrentFilters(uuid)
        }
    }

    @PostMapping("/filter")
    fun setFilter(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: MatchFilterRequest
    ): ResponseEntity<MatchFilterResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/match/filter",
            metaUuid = uuid.toString(),
            errorMapper = { getMatchFilterErrorResponse(it) }
        ) {
            matchesService.setFilter(userId = uuid, request = request)
        }
    }
}