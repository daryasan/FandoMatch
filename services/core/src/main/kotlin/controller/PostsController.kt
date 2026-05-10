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
        @RequestHeader("Authorization", required = false) authorization: String?,
        @RequestBody request: PostsGetRequest
    ): ResponseEntity<PostListResponse> {
        val viewerUserId = authorization?.let { runCatching { tokenParserService.parse(it).userId }.getOrNull() }
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/posts/get",
            metaUuid = request.uuid,
            errorMapper = { getPostListErrorResponse(it) }
        ) {
            postsService.getPosts(request.uuid, request.pagination?.cursorTimestamp, request.pagination?.propertySize, viewerUserId)
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
        @RequestHeader("Authorization", required = false) authorization: String?,
        @PathVariable("post_id") postId: String
    ): ResponseEntity<ExtendedPostResponse> {
        val viewerUserId = authorization?.let { runCatching { tokenParserService.parse(it).userId }.getOrNull() }
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/posts/$postId",
            metaUuid = postId,
            errorMapper = { getExtendedPostErrorResponse(it) }
        ) {
            postsService.getPost(postId, viewerUserId)
        }
    }

    @GetMapping("/{post_id}/comments")
    fun getComments(
        @PathVariable("post_id") postId: String,
        @RequestBody request: CommentsGetRequest
    ): ResponseEntity<CommentListResponse> {
        return onControllerRequest(
            logger = logger,
            operationName = "GET /core/posts/$postId/comments",
            metaUuid = postId,
            errorMapper = { getCommentListErrorResponse(it) }
        ) {
            postsService.getComments(postId, request.cursorTimestamp, request.propertySize)
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

    @PostMapping("/{post_id}/comment")
    fun sendComment(
        @RequestHeader("Authorization") token: String,
        @PathVariable("post_id") postId: String,
        @RequestBody request: CreateCommentRequest
    ): ResponseEntity<CreateCommentResponse> {
        val uuid = tokenParserService.parse(token).userId
        return onControllerRequest(
            logger = logger,
            operationName = "POST /core/posts/$postId/comment",
            metaUuid = uuid.toString(),
            errorMapper = { getCreateCommentErrorResponse(it) }
        ) {
            postsService.sendComment(uuid, postId, request.content)
        }
    }
}
