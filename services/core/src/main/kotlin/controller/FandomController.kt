package org.example.controller

import com.fandomatch.core.model.*
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.FandomService
import org.example.util.getFandomCategoryListErrorResponse
import org.example.util.getFandomListErrorResponse
import org.example.util.getFandomOneshotErrorResponse
import org.example.util.getFandomRequestCreateErrorResponse
import org.example.util.onControllerRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/core/fandoms")
class FandomController(
    private val fandomService: FandomService,
    @Value("\${service.api-key}") private val serviceApiKey: String,
) {

    companion object : KLogging()

    @PostMapping("/user")
    fun getUserFandoms(
        @RequestBody request: FandomsGetRequest
    ): ResponseEntity<FandomListResponse> {
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/fandoms/user",
            metaUuid = request.uuid,
            errorMapper = { getFandomListErrorResponse(it) }
        ) {
            fandomService.getUserFandoms(request.uuid)
        }
    }

    @GetMapping("/categories")
    fun getCategories(): ResponseEntity<FandomCategoryListResponse> {
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/fandoms/categories",
            errorMapper = { getFandomCategoryListErrorResponse(it) }
        ) {
            fandomService.getCategories()
        }
    }

    @PostMapping("/request-new")
    fun requestNewFandom(
        @RequestBody request: FandomRequestCreate
    ): ResponseEntity<FandomRequestCreateResponse> {
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/fandoms/request-new",
            metaUuid = request.authorUuid,
            errorMapper = { getFandomRequestCreateErrorResponse(it) }
        ) {
            fandomService.requestNewFandom(request)
        }
    }

    @GetMapping("/search")
    fun searchFandoms(
        @RequestParam query: String
    ): ResponseEntity<FandomListResponse> {
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/fandoms/search",
            errorMapper = { getFandomListErrorResponse(it) }
        ) {
            fandomService.searchFandoms(query)
        }
    }

    @PostMapping("/fandom_oneshot")
    fun fandomOneshot(
        @RequestHeader("X-Api-Key") apiKey: String,
        @RequestBody request: FandomOneshotRequest
    ): ResponseEntity<FandomOneshotResponse> {
        if (apiKey != serviceApiKey) return ResponseEntity.status(403).build()
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/fandoms/fandom_oneshot",
            errorMapper = { getFandomOneshotErrorResponse(it) }
        ) {
            fandomService.fandomOneshot(request)
        }
    }
}
