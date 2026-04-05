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
            metaUuid = request.username,
            errorMapper = { getFandomListErrorResponse(it) }
        ) {
            fandomService.getUserFandoms(request.username)
        }
    }

    @GetMapping("/all")
    fun getAllFandoms(
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<FandomListResponse> {
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/fandoms/all",
            errorMapper = { getFandomListErrorResponse(it) }
        ) {
            fandomService.getAllFandomsPaginated(page, size)
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
            metaUuid = request.authorUsername,
            errorMapper = { getFandomRequestCreateErrorResponse(it) }
        ) {
            fandomService.requestNewFandom(request)
        }
    }
}
