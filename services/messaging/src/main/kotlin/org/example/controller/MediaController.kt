package org.example.controller

import com.fandomatch.messaging.model.*
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.MediaPresignService
import org.example.util.getPresignedUploadErrorResponse
import org.example.util.onControllerRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/messaging/media")
class MediaController(
    private val mediaPresignService: MediaPresignService
) {

    companion object : KLogging()

    @PostMapping("/presigned-upload")
    fun getPresignedUploadUrl(
        @RequestHeader("Authorization", required = false) token: String?,
        @RequestBody request: PresignedUploadRequest
    ): ResponseEntity<PresignedUploadResponse> {
        return onControllerRequest(
            logger = logger,
            operationName = "POST /messaging/media/presigned-upload",
            metaUuid = null,
            errorMapper = { getPresignedUploadErrorResponse(it) }
        ) {
            mediaPresignService.generateUploadUrl(request.mediaType)
        }
    }
}
