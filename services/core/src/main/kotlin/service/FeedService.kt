package org.example.service

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import org.example.exceptions.UserNotFoundException
import org.example.repository.MatchRepository
import org.example.repository.PostLikeRepository
import org.example.repository.PostRepository
import org.example.repository.UserProfileRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.util.*

@Service
class FeedService(
    private val matchRepository: MatchRepository,
    private val postRepository: PostRepository,
    private val userProfileRepository: UserProfileRepository,
    private val mediaService: MediaService,
    private val postLikeRepository: PostLikeRepository,
) {

    companion object {
        private const val DEFAULT_PAGE = 0
        private const val DEFAULT_SIZE = 20
    }

    fun getFeed(userId: UUID, page: Int?, size: Int?): PostListResponse {
        val matches = matchRepository.getAllUserMatches(userId)

        val matchedUserIds = matches.flatMap { match ->
            listOf(match.userId1, match.userId2)
        }.filter { it != userId }.distinct()

        val posts = if (matchedUserIds.isEmpty()) {
            emptyList()
        } else {
            val pageable = PageRequest.of(page ?: DEFAULT_PAGE, size ?: DEFAULT_SIZE)
            postRepository.findByAuthorIdInOrderByCreatedAtDesc(matchedUserIds, pageable)
        }

        return PostListResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = PostListData(posts.map { it.toDto() })
        )
    }

    private fun org.example.models.db_models.Post.toDto(): Post {
        val author = userProfileRepository.findById(authorId).orElseThrow { UserNotFoundException(authorId.toString()) }
        return Post(
            id = id.toString(),
            title = title,
            content = content,
            createdAt = createdAt.epochSecond,
            author = PostAuthor(
                username = author.username,
                uuid = author.userId.toString(),
                name = author.name,
                avatar = MediaItem(
                    mediaId = author.avatarMediaId.toString(),
                    mediaType = MediaType.IMAGE,
                    url = mediaService.generateSignedDownloadUrl(author.avatarMediaId.toString())
                )
            ),
            likeCount = postLikeRepository.getPostLikeCount(id),
            commentCount = ,
            fandoms = TODO(),
            mediaItems = TODO()
        )
    }
}
