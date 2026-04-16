package org.example.controller

import com.fandomatch.core.model.*
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.FandomService
import org.example.util.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/core/fandoms")
class FandomController(
    private val fandomService: FandomService
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
}
