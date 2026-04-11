package org.example.controller

import com.fandomatch.messaging.model.*
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.MediaPresignService
import org.example.service.TokenParserService
import org.example.util.getPresignedUploadErrorResponse
import org.example.util.onControllerRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/messaging/media")
class MediaController(
    private val mediaPresignService: MediaPresignService,
    private val tokenParserService: TokenParserService
) {

    companion object : KLogging()

    @PostMapping("/presigned-upload")
    fun getPresignedUploadUrl(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: PresignedUploadRequest
    ): ResponseEntity<PresignedUploadResponse> {
        val userId = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /messaging/media/presigned-upload",
            metaUuid = userId.toString(),
            errorMapper = { getPresignedUploadErrorResponse(it) }
        ) {
            mediaPresignService.generateUploadUrl(request.mediaType)
        }
    }
}
