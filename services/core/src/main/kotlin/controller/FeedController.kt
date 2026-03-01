package org.example.controller

import com.fandomatch.core.model.CreatePostResponse
import com.fandomatch.core.model.PostListResponse
import io.github.oshai.kotlinlogging.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime

@RestController
@RequestMapping("/core")
class FeedController {

    companion object : KLogging()

    @GetMapping("/feed")
    fun getFeed(
        @RequestHeader("Authorization") token: String,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<PostListResponse> {
        logger.info { "GET /core/feed called (page=$page, size=$size)" }

        val stubPost = CreatePostResponse(
            id = "stub-id",
            title = "Feed Stub Post",
            content = "Stub content from feed",
            createdAt = OffsetDateTime.now()
        )

        val response = PostListResponse(
            posts = listOf(stubPost)
        )

        logger.info { "GET /core/feed returning 200" }
        return ResponseEntity.ok(response)
    }
}