package org.example.service

import com.fandomatch.core.model.MatchCandidateBatchData
import com.fandomatch.core.model.MatchCandidateBatchResponse
import com.fandomatch.core.model.ResponseStatus
import org.example.models.db_models.MatchFilter
import org.example.repository.*
import org.example.util.toMatchCandidateResponse
import org.example.util.toMatchPending
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.min

@Service
class MatchesService(
    private val matchRepository: MatchRepository,
    private val userProfilesRepository: UserProfileRepository,
    private val matchFilterRepository: MatchFilterRepository,
    private val fandomService: FandomService,
    private val matchPendingRepository: MatchPendingRepository,
    private val matchActionRepository: MatchActionRepository,
) {

    companion object {
        const val LIKE = "LIKE"
    }

    fun areFriends(user1: UUID, user2: UUID): Boolean {
        val (first, second) = if (user1 < user2) user1 to user2 else user2 to user1
        return matchRepository.existsByUserId1AndUserId2(first, second)
    }

    // TODO make them NOT random
    fun getNextCandidates(userId: UUID, batchSize: Int): MatchCandidateBatchResponse {
        val filters = matchFilterRepository.findById(userId).orElse(MatchFilter(userId = userId))

        val candidates = userProfilesRepository.findCandidates(
            userId = userId,
            gender = filters.gender,
            city = filters.city,
            ageFrom = filters.ageFrom,
            ageTo = filters.ageTo,
            fandomId = filters.fandomId,
            pageable = PageRequest.of(0, batchSize)
        )


        val suggested = candidates.map { candidate ->
            val candidateFandoms = fandomService.getFandoms(candidate.userId)
            candidate.toMatchCandidateResponse(
                compatibility = calculateCompatibility(userId, candidate.userId),
                fandoms = candidateFandoms,
            )
        }

        matchPendingRepository.save(suggested.first().toMatchPending(userId))
        return MatchCandidateBatchResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = MatchCandidateBatchData(suggested)
        )
    }

//    @Transactional
//    fun react(userId: UUID, targetUsername: String, action: String): ReactResult {
//        val targetProfile = userProfilesService.findByUsername(targetUsername)
//        val targetUserId = targetProfile.userId
//
//        val matchAction = MatchAction(
//            userId = userId,
//            targetUserId = targetUserId,
//            action = action
//        )
//        matchActionRepository.save(matchAction)
//        matchPendingRepository.deleteByUserIdAndSuggestedUserId(userId, targetUserId)
//
//        val oppositeAction = hasTargetUserLikedCurrentUser(targetUserId, userId)
//        val isMutual = oppositeAction?.action == LIKE && action == LIKE
//
//        return if (isMutual) {
//            // 5. Создаём мэтч
//            val (first, second) = if (userId < targetUserId) userId to targetUserId else targetUserId to userId
//            val match = Match(userId1 = first, userId2 = second)
//            matchRepository.save(match)
//
//            // 6. Публикуем событие в Kafka для Messaging Service
//            kafkaTemplate.send(
//                "match-events", mapOf(
//                    "matchId" to match.id,
//                    "user1Id" to first,
//                    "user2Id" to second
//                )
//            )
//
//            ReactResult.Match(match.id)
//        } else {
//            when (action) {
//                "LIKE" -> ReactResult.Liked
//                "DISLIKE" -> ReactResult.Disliked
//                else -> throw IllegalArgumentException("Invalid action: $action")
//            }
//        }
//    }

    fun hasTargetUserLikedCurrentUser(currentUserId: UUID, targetUserId: UUID) =
        matchActionRepository.findByUserIdAndTargetUserId(targetUserId, currentUserId)

    private fun calculateCompatibility(currentUserId: UUID, candidateId: UUID): Double {
        val currentUserFandoms = fandomService.getFandoms(currentUserId).toSet()
        val candidateFandoms = fandomService.getFandoms(candidateId).toSet()

        val common = currentUserFandoms.intersect(candidateFandoms).size
        return if (currentUserFandoms.isNotEmpty() && candidateFandoms.isNotEmpty()) {
            (common * 100).toDouble() / (min(candidateFandoms.size, candidateFandoms.size))
        } else 0.0
    }
}