package org.example.service

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import jakarta.transaction.Transactional
import org.example.exceptions.FandomCategoryNotFoundException
import org.example.exceptions.PostNotFoundException
import org.example.exceptions.UserNotFoundException
import org.example.models.db_models.MediaItemRecord
import org.example.models.db_models.PostLike
import org.example.repository.CommentRepository
import org.example.repository.FandomCategoryRepository
import org.example.repository.FandomRepository
import org.example.repository.MediaItemRepository
import org.example.repository.PostLikeRepository
import org.example.repository.PostRepository
import org.example.repository.UserProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class PostsService(
    private val postRepository: PostRepository,
    private val postLikeRepository: PostLikeRepository,
    private val commentRepository: CommentRepository,
    private val userProfileRepository: UserProfileRepository,
    private val mediaService: MediaService,
    private val mediaItemRepository: MediaItemRepository,
    private val fandomRepository: FandomRepository,
    private val fandomCategoryRepository: FandomCategoryRepository,
) {

    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 20
    }

    fun getPosts(uuid: String, cursorTimestamp: Long?, size: Int?, viewerUserId: UUID? = null): PostListResponse {
        val userId = UUID.fromString(uuid)
        if (!userProfileRepository.existsById(userId)) {
            throw UserNotFoundException(uuid)
        }

        val pageable = PageRequest.of(DEFAULT_PAGE, size ?: DEFAULT_SIZE)
        val posts = if (cursorTimestamp != null) {
            val cursor = Instant.ofEpochSecond(cursorTimestamp)
            postRepository.findByAuthorIdAndCreatedAtBeforeOrderByCreatedAtDesc(userId, cursor, pageable)
        } else {
            postRepository.findByAuthorIdOrderByCreatedAtDesc(userId, pageable)
        }

        return PostListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = PostListData(posts.map { it.toPost(viewerUserId) })
        )
    }

    @Transactional
    fun createPost(userId: UUID, request: CreatePostRequest): CreatePostResponse {
        val mediaInputs = request.mediaItems ?: emptyList()

        val post = org.example.models.db_models.Post(
            authorId = userId,
            fandomIds = request.fandomIds?.toTypedArray() ?: emptyArray(),
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
            successResponse = saved.toPost(userId)
        )
    }

    fun getPost(postId: String, viewerUserId: UUID? = null): ExtendedPostResponse {
        val postUuid = UUID.fromString(postId)
        val post = postRepository.findById(postUuid)
            .orElseThrow { PostNotFoundException(postId) }

        val pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE)
        val comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postUuid, pageable)

        return ExtendedPostResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = post.toExtendedDto(comments, viewerUserId)
        )
    }

    fun getComments(postId: String, cursorTimestamp: Long?, size: Int?): CommentListResponse {
        val postUuid = UUID.fromString(postId)

        if (!postRepository.existsById(postUuid)) {
            throw PostNotFoundException(postId)
        }

        val pageable = PageRequest.of(DEFAULT_PAGE, size ?: DEFAULT_SIZE)
        val comments = if (cursorTimestamp != null) {
            val cursor = Instant.ofEpochSecond(cursorTimestamp)
            commentRepository.findByPostIdAndCreatedAtBeforeOrderByCreatedAtAsc(postUuid, cursor, pageable)
        } else {
            commentRepository.findByPostIdOrderByCreatedAtAsc(postUuid, pageable)
        }

        return CommentListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = CommentListData(comments.map { it.toCommentDto() })
        )
    }

    @Transactional
    fun sendComment(userId: UUID, postId: String, content: String): CreateCommentResponse {
        val postUuid = UUID.fromString(postId)

        if (!postRepository.existsById(postUuid)) {
            throw PostNotFoundException(postId)
        }

        val comment = org.example.models.db_models.Comment(
            postId = postUuid,
            authorId = userId,
            content = content,
        )
        val saved = commentRepository.save(comment)

        return CreateCommentResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = saved.toCommentDto()
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

    private fun org.example.models.db_models.Comment.toCommentDto(): Comment {
        val profile = userProfileRepository.findById(authorId).orElse(null)
        return Comment(
            id = id!!.toString(),
            authorUsername = profile?.username ?: "unknown",
            authorName = profile?.name,
            authorAvatar = profile?.avatarMediaId?.let { mediaId ->
                MediaItem(mediaId = mediaId, mediaType = MediaType.IMAGE, url = mediaService.generateSignedDownloadUrl(mediaId))
            },
            content = content,
            createdAt = createdAt.epochSecond
        )
    }

    private fun org.example.models.db_models.Post.toExtendedDto(
        comments: List<org.example.models.db_models.Comment>,
        viewerUserId: UUID? = null
    ): ExtendedPost {
        val base = toExtendedPost(viewerUserId)
        return ExtendedPost(
            id = base.id,
            content = base.content,
            author = base.author,
            createdAt = base.createdAt,
            likeCount = base.likeCount,
            commentCount = base.commentCount,
            isLikedByCurrentUser = base.isLikedByCurrentUser,
            fandoms = base.fandoms,
            mediaItems = base.mediaItems,
            comments = comments.map { it.toCommentDto() }
        )
    }

    private fun org.example.models.db_models.Post.toPost(viewerUserId: UUID? = null): Post {
        val author = userProfileRepository.findById(authorId)
            .orElseThrow { UserNotFoundException(authorId.toString()) }

        val safeFandomIds = fandomIds ?: emptyArray()
        val safeMediaIds = mediaIds ?: emptyArray()

        val mediaTypeMap = if (safeMediaIds.isNotEmpty()) {
            mediaItemRepository.findAllByMediaIdIn(safeMediaIds.toList()).associate { it.mediaId to it.mediaType }
        } else {
            emptyMap()
        }

        val fandoms = if (safeFandomIds.isNotEmpty()) {
            fandomRepository.findAllById(safeFandomIds.map { UUID.fromString(it) }).map { fandom ->
                val category = fandomCategoryRepository.findById(fandom.categoryId)
                    .orElseThrow { FandomCategoryNotFoundException(fandom.categoryId.toString()) }
                Fandom(
                    id = fandom.id!!.toString(),
                    name = fandom.name,
                    category = category.let { FandomCategory.valueOf(it.name) }
                )
            }
        } else null

        return Post(
            id = id!!.toString(),
            content = content,
            createdAt = createdAt.epochSecond,
            author = PostAuthor(
                username = author.username,
                uuid = author.userId.toString(),
                name = author.name ?: author.username,
                avatar = author.avatarMediaId?.let { mediaId ->
                    MediaItem(
                        mediaId = mediaId,
                        mediaType = MediaType.IMAGE,
                        url = mediaService.generateSignedDownloadUrl(mediaId)
                    )
                }
            ),
            likeCount = postLikeRepository.getPostLikeCount(id),
            commentCount = commentRepository.countByPostId(id).toInt(),
            isLikedByCurrentUser = viewerUserId?.let { postLikeRepository.existsByUserIdAndPostId(it, id!!) } ?: false,
            fandoms = fandoms,
            mediaItems = safeMediaIds.map { mediaId ->
                val typeStr = mediaTypeMap[mediaId]
                val mediaType = typeStr?.let { t -> MediaType.entries.firstOrNull { it.value == t } }
                    ?: MediaType.IMAGE
                MediaItem(mediaId = mediaId, mediaType = mediaType, url = mediaService.generateSignedDownloadUrl(mediaId))
            }.ifEmpty { null }
        )
    }

    private fun org.example.models.db_models.Post.toExtendedPost(viewerUserId: UUID? = null): ExtendedPost {
        val author = userProfileRepository.findById(authorId)
            .orElseThrow { UserNotFoundException(authorId.toString()) }

        val safeFandomIds = fandomIds ?: emptyArray()
        val safeMediaIds = mediaIds ?: emptyArray()

        val mediaTypeMap = if (safeMediaIds.isNotEmpty()) {
            mediaItemRepository.findAllByMediaIdIn(safeMediaIds.toList()).associate { it.mediaId to it.mediaType }
        } else {
            emptyMap()
        }

        val fandoms = if (safeFandomIds.isNotEmpty()) {
            fandomRepository.findAllById(safeFandomIds.map { UUID.fromString(it) }).map { fandom ->
                val category = fandomCategoryRepository.findById(fandom.categoryId)
                    .orElseThrow { FandomCategoryNotFoundException(fandom.categoryId.toString()) }
                Fandom(
                    id = fandom.id!!.toString(),
                    name = fandom.name,
                    category = category.let { FandomCategory.valueOf(it.name) }
                )
            }
        } else null

        return ExtendedPost(
            id = id!!.toString(),
            content = content,
            createdAt = createdAt.epochSecond,
            author = PostAuthor(
                username = author.username,
                uuid = author.userId.toString(),
                name = author.name ?: author.username,
                avatar = author.avatarMediaId?.let { mediaId ->
                    MediaItem(
                        mediaId = mediaId,
                        mediaType = MediaType.IMAGE,
                        url = mediaService.generateSignedDownloadUrl(mediaId)
                    )
                }
            ),
            likeCount = postLikeRepository.getPostLikeCount(id!!),
            commentCount = commentRepository.countByPostId(id!!).toInt(),
            isLikedByCurrentUser = viewerUserId?.let { postLikeRepository.existsByUserIdAndPostId(it, id!!) } ?: false,
            fandoms = fandoms,
            mediaItems = safeMediaIds.map { mediaId ->
                val typeStr = mediaTypeMap[mediaId]
                val mediaType = typeStr?.let { t -> MediaType.values().firstOrNull { it.value == t } }
                    ?: MediaType.IMAGE
                MediaItem(mediaId = mediaId, mediaType = mediaType, url = mediaService.generateSignedDownloadUrl(mediaId))
            }.ifEmpty { null }
        )
    }
}
