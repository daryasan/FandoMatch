package org.example.controller

import com.fandomatch.core.model.*
import com.fandomatch.core.model.ResponseStatus
import io.github.oshai.kotlinlogging.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/core/match")
class MatchController {

    companion object : KLogging()

    @PostMapping("/next")
    fun getNextBatch(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: MatchBatchRequest
    ): ResponseEntity<MatchCandidateBatchResponse> {
        logger.info { "POST /core/match/next called (batch_size=${request.batchSize})" }

        val response = MatchCandidateBatchResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = MatchCandidateBatchData(emptyList())
        )

        logger.info { "POST /core/match/next returning 200" }
        return ResponseEntity.ok(response)
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