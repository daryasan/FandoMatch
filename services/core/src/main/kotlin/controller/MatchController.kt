package org.example.controller

import com.fandomatch.core.model.*
import com.fandomatch.core.model.ResponseStatus
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.MatchesService
import org.example.service.TokenParserService
import org.example.util.getMatchCandidateBatchErrorResponse
import org.example.util.onControllerRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/core/match")
class MatchController(
    private val matchesService: MatchesService,
    private val tokenParserService: TokenParserService,
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
            val suggested = matchesService.getNextCandidates(userId = uuid, batchSize = request.batchSize)
            return ResponseEntity.ok(suggested)
        }
    }

    @PostMapping("/react")
    fun react(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: MatchActionRequest
    ): ResponseEntity<MatchActionResponse> {
        logger.info { "POST /core/match/react called, target=${request.targetUsername}, action=${request.action}" }

        val response = MatchActionResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = MatchActionResult(
                status = MatchActionResult.Status.LIKED,
                matchChatId = null
            )
        )

        logger.info { "POST /core/match/react returning 200" }
        return ResponseEntity.ok(response)
    }

    @PostMapping("/filter")
    fun setFilter(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: MatchFilterRequest
    ): ResponseEntity<MatchFilterResponse> {
        logger.info { "POST /core/match/filter called" }

        val response = MatchFilterResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = MatchActionResult(
                status = MatchActionResult.Status.LIKED,
                matchChatId = null
            )
        )

        logger.info { "POST /core/match/filter returning 200" }
        return ResponseEntity.ok(response)
    }
}