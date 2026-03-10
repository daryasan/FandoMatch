package org.example.controller

import com.fandomatch.core.model.*
import com.fandomatch.core.model.ResponseStatus
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.PostsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/core/posts")
class PostsController(
    private val postsService: PostsService
) {

    companion object : KLogging()

    @PostMapping("/get")
    fun getPosts(
        @RequestBody request: PostsGetRequest
    ): ResponseEntity<PostListResponse> {
        logger.info { "POST /core/posts/get called with username=${request.username}" }

        val response = PostListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = PostListData(emptyList())
        )

        logger.info { "POST /core/posts/get returning 200" }
        return ResponseEntity.ok(response)
    }

    @PostMapping("/create")
    fun createPost(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: CreatePostRequest
    ): ResponseEntity<CreatePostResponse> {
        logger.info { "POST /core/posts/create called, title=${request.title}" }

        val response = CreatePostResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = Post(
                id = "stub-id",
                title = request.title,
                content = request.content,
                createdAt = java.time.OffsetDateTime.now()
            )
        )

        logger.info { "POST /core/posts/create returning 200" }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{post_id}")
    fun getPost(
        @PathVariable("post_id") postId: String
    ): ResponseEntity<CreatePostResponse> {
        logger.info { "GET /core/posts/$postId called" }

        val response = CreatePostResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = Post(
                id = postId,
                title = "Stub title",
                content = "Stub content",
                createdAt = java.time.OffsetDateTime.now()
            )
        )

        logger.info { "GET /core/posts/$postId returning 200" }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{post_id}/comments")
    fun getComments(
        @PathVariable("post_id") postId: String,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<CommentListResponse> {
        logger.info { "GET /core/posts/$postId/comments called (page=$page, size=$size)" }

        val response = CommentListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = CommentListData(emptyList())
        )

        logger.info { "GET /core/posts/$postId/comments returning 200" }
        return ResponseEntity.ok(response)
    }

    @PostMapping("/{post_id}/like")
    fun likePost(
        @RequestHeader("Authorization") token: String,
        @PathVariable("post_id") postId: String
    ): ResponseEntity<PostLikeResponse> {
        logger.info { "POST /core/posts/$postId/like called" }

        val response = PostLikeResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = PostLikeSuccess(status = PostLikeSuccess.Status.LIKED)
        )

        logger.info { "POST /core/posts/$postId/like returning 200" }
        return ResponseEntity.ok(response)
    }
}