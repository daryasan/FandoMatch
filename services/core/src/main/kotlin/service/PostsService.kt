package org.example.service

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import jakarta.transaction.Transactional
import org.example.exceptions.PostNotFoundException
import org.example.exceptions.UserNotFoundException
import org.example.models.db_models.MediaItemRecord
import org.example.models.db_models.PostLike
import org.example.repository.CommentRepository
import org.example.repository.MediaItemRepository
import org.example.repository.PostLikeRepository
import org.example.repository.PostRepository
import org.example.repository.UserProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Service
class PostsService(
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val commentRepository: CommentRepository,
    private val userProfileRepository: UserProfileRepository,
    private val mediaService: MediaService,
    private val mediaItemRepository: MediaItemRepository
) {

    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 20
    }

    fun getPosts(username: String, page: Int?, size: Int?): PostListResponse {
        val userProfile = userProfileRepository.findByUsername(username)
            .orElseThrow { UserNotFoundException(username) }

        val pageable = PageRequest.of(page ?: DEFAULT_PAGE, size ?: DEFAULT_SIZE)
        val posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(userProfile.userId, pageable)

        return PostListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = PostListData(posts.map { it.toDto() })
        )
    }

    @Transactional
    fun createPost(userId: UUID, request: CreatePostRequest): CreatePostResponse {
        val mediaInputs = request.mediaItems ?: emptyList()

        val post = org.example.models.db_models.Post(
            authorId = userId,
            fandomId = request.fandomId?.let { UUID.fromString(it) },
            title = request.title,
            content = request.content,
            mediaIds = mediaInputs.map { it.mediaId }.toTypedArray()
        )

        val saved = postRepository.save(post)

        if (mediaInputs.isNotEmpty()) {
            mediaItemRepository.saveAll(
                mediaInputs.map { MediaItemRecord(mediaId = it.mediaId, mediaType = it.mediaType.value) }
            )
        }

        return CreatePostResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = saved.toDto()
        )
    }

    fun getPost(postId: String): CreatePostResponse {
        val post = postRepository.findById(UUID.fromString(postId))
            .orElseThrow { PostNotFoundException(postId) }

        return CreatePostResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = post.toDto()
        )
    }

    fun getComments(postId: String, page: Int?, size: Int?): CommentListResponse {
        val postUuid = UUID.fromString(postId)

        if (!postRepository.existsById(postUuid)) {
            throw PostNotFoundException(postId)
        }

        val pageable = PageRequest.of(page ?: DEFAULT_PAGE, size ?: DEFAULT_SIZE)
        val comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postUuid, pageable)

        val authorIds = comments.map { it.authorId }.distinct()
        val usernamesByUserId = userProfileRepository.findAllById(authorIds)
            .associate { it.userId to it.username }

        val commentDtos = comments.map { comment ->
            CommentListDataCommentsInner(
                id = comment.id.toString(),
                author = usernamesByUserId[comment.authorId] ?: "unknown",
                content = comment.content,
                createdAt = comment.createdAt.atOffset(ZoneOffset.UTC)
            )
        }

        return CommentListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = CommentListData(commentDtos)
        )
    }

    @Transactional
    fun likePost(userId: UUID, postId: String): PostLikeResponse {
        val postUuid = UUID.fromString(postId)

        if (!postRepository.existsById(postUuid)) {
            throw PostNotFoundException(postId)
        }

        if (!postLikeRepository.existsByUserIdAndPostId(userId, postUuid)) {
            postLikeRepository.save(PostLike(userId = userId, postId = postUuid))
        }

        return PostLikeResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = PostLikeSuccess(status = PostLikeSuccess.Status.LIKED)
        )
    }

    private fun org.example.models.db_models.Post.toDto(): Post {
        val mediaTypeMap = if (mediaIds.isNotEmpty()) {
            mediaItemRepository.findAllByMediaIdIn(mediaIds.toList()).associate { it.mediaId to it.mediaType }
        } else {
            emptyMap()
        }

        return Post(
            id = id.toString(),
            title = title,
            content = content,
            createdAt = createdAt.atOffset(ZoneOffset.UTC),
            mediaItems = mediaIds.map { mediaId ->
                val typeStr = mediaTypeMap[mediaId]
                val mediaType = typeStr?.let { t -> MediaType.values().firstOrNull { it.value == t } }
                    ?: MediaType.IMAGE
                MediaItem(mediaId = mediaId, mediaType = mediaType, url = mediaService.generateSignedDownloadUrl(mediaId))
            }.ifEmpty { null }
        )
    }
}
