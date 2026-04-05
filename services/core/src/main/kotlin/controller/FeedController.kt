package org.example.controller

import com.fandomatch.core.model.PostListResponse
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.FeedService
import org.example.service.TokenParserService
import org.example.util.getPostListErrorResponse
import org.example.util.onControllerRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/core")
class FeedController(
    private val feedService: FeedService,
    private val tokenParserService: TokenParserService
) {

    companion object : KLogging()

    @GetMapping("/feed")
    fun getFeed(
        @RequestHeader("Authorization") token: String,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<PostListResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/feed",
            metaUuid = uuid.toString(),
            errorMapper = { getPostListErrorResponse(it) }
        ) {
            feedService.getFeed(uuid, page, size)
        }
    }
}
