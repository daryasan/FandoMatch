package org.example.controller

import com.fandomatch.core.model.*
import io.github.oshai.kotlinlogging.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/core/fandoms")
class FandomController {

    companion object : KLogging()

    @PostMapping("/user")
    fun getUserFandoms(
        @RequestBody request: FandomsGetRequest
    ): ResponseEntity<FandomListResponse> {
        logger.info { "POST /core/fandoms/user called for username=${request.username}" }

        val response = FandomListResponse(fandoms = emptyList())

        logger.info { "POST /core/fandoms/user returning 200" }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/all")
    fun getAllFandoms(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<FandomListResponse> {
        logger.info { "GET /core/fandoms/all called (page=$page, size=$size)" }

        val response = FandomListResponse(fandoms = emptyList())

        logger.info { "GET /core/fandoms/all returning 200" }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/categories")
    fun getCategories(): ResponseEntity<FandomCategoryListResponse> {
        logger.info { "GET /core/fandoms/categories called" }

        val response = FandomCategoryListResponse(categories = emptyList())

        logger.info { "GET /core/fandoms/categories returning 200" }
        return ResponseEntity.ok(response)
    }

    @PostMapping("/request-new")
    fun requestNewFandom(
        @RequestBody request: FandomRequestCreate
    ): ResponseEntity<FandomRequestCreateResponse> {
        logger.info { "POST /core/fandoms/request-new called, name=${request.name}" }

        val response = FandomRequestCreateResponse(
            status = FandomRequestCreateResponse.Status.RECEIVED
        )

        logger.info { "POST /core/fandoms/request-new returning 200" }
        return ResponseEntity.ok(response)
    }
}