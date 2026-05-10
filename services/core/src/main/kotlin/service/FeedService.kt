package org.example.service

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import org.example.exceptions.FandomCategoryNotFoundException
import org.example.exceptions.UserNotFoundException
import org.example.repository.CommentRepository
import org.example.repository.FandomCategoryRepository
import org.example.repository.FandomRepository
import org.example.repository.MatchRepository
import org.example.repository.MediaItemRepository
import org.example.repository.PostLikeRepository
import org.example.repository.PostRepository
import org.example.repository.UserProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*

@Service
class FeedService(
    private val matchRepository: MatchRepository,
    private val postRepository: PostRepository,
    private val userProfileRepository: UserProfileRepository,
    private val mediaService: MediaService,
    private val postLikeRepository: PostLikeRepository,
    private val commentRepository: CommentRepository,
    private val mediaItemRepository: MediaItemRepository,
    private val fandomRepository: FandomRepository,
    private val fandomCategoryRepository: FandomCategoryRepository,
) {

    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 20
    }

    fun getFeed(userId: UUID, cursorTimestamp: Long?, size: Int): PostListResponse {
        val matches = matchRepository.getAllUserMatches(userId)

        val matchedUserIds = matches.flatMap { match ->
            listOf(match.userId1, match.userId2)
        }.filter { it != userId }.distinct()

        val posts = if (matchedUserIds.isEmpty()) {
            emptyList()
        } else {
            val pageable = PageRequest.of(DEFAULT_PAGE, size)
            if (cursorTimestamp != null) {
                val cursor = Instant.ofEpochSecond(cursorTimestamp)
                postRepository.findByAuthorIdInAndCreatedAtBeforeOrderByCreatedAtDesc(matchedUserIds, cursor, pageable)
            } else {
                postRepository.findByAuthorIdInOrderByCreatedAtDesc(matchedUserIds, pageable)
            }
        }

        return PostListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = PostListData(posts.map { it.toDto(userId) })
        )
    }

    private fun org.example.models.db_models.Post.toDto(viewerUserId: UUID): Post {
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
                val category = fandomCategoryRepository.findById(fandom.categoryId).orElseThrow { FandomCategoryNotFoundException(fandom.categoryId.toString()) }
                Fandom(
                    id = fandom.id.toString(),
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
            likeCount = postLikeRepository.getPostLikeCount(id!!),
            commentCount = commentRepository.countByPostId(id!!).toInt(),
            isLikedByCurrentUser = postLikeRepository.existsByUserIdAndPostId(viewerUserId, id!!),
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
