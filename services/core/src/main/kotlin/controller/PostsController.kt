package org.example.controller

import com.fandomatch.core.model.*
import io.github.oshai.kotlinlogging.KLogging
import org.example.service.PostsService
import org.example.service.TokenParserService
import org.example.util.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/core/posts")
class PostsController(
    private val postsService: PostsService,
    private val tokenParserService: TokenParserService
) {

    companion object : KLogging()

    @PostMapping("/get")
    fun getPosts(
        @RequestBody request: PostsGetRequest
    ): ResponseEntity<PostListResponse> {
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/posts/get",
            metaUuid = request.username,
            errorMapper = { getPostListErrorResponse(it) }
        ) {
            postsService.getPosts(request.username, request.page, request.propertySize)
        }
    }

    @PostMapping("/create")
    fun createPost(
        @RequestHeader("Authorization") token: String,
        @RequestBody request: CreatePostRequest
    ): ResponseEntity<CreatePostResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/posts/create",
            metaUuid = uuid.toString(),
            errorMapper = { getCreatePostErrorResponse(it) }
        ) {
            postsService.createPost(uuid, request)
        }
    }

    @GetMapping("/{post_id}")
    fun getPost(
        @PathVariable("post_id") postId: String
    ): ResponseEntity<CreatePostResponse> {
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/posts/$postId",
            metaUuid = postId,
            errorMapper = { getCreatePostErrorResponse(it) }
        ) {
            postsService.getPost(postId)
        }
    }

    @GetMapping("/{post_id}/comments")
    fun getComments(
        @PathVariable("post_id") postId: String,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) size: Int?
    ): ResponseEntity<CommentListResponse> {
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/posts/$postId/comments",
            metaUuid = postId,
            errorMapper = { getCommentListErrorResponse(it) }
        ) {
            postsService.getComments(postId, page, size)
        }
    }

    @PostMapping("/{post_id}/like")
    fun likePost(
        @RequestHeader("Authorization") token: String,
        @PathVariable("post_id") postId: String
    ): ResponseEntity<PostLikeResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/posts/$postId/like",
            metaUuid = uuid.toString(),
            errorMapper = { getPostLikeErrorResponse(it) }
        ) {
            postsService.likePost(uuid, postId)
        }
    }
}
