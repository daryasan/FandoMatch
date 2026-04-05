package org.example.service

import com.fandomatch.core.model.*
import jakarta.transaction.Transactional
import org.example.exceptions.AlreadyReactedException
import org.example.exceptions.UserNotFoundException
import org.example.models.db_models.Match
import org.example.models.db_models.MatchAction
import org.example.models.db_models.MatchFilter
import org.example.repository.*
import org.example.stream.out.MatchEventProducer
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
    private val matchEventProducer: MatchEventProducer,
    private val matchActionRepository: MatchActionRepository
) {
    companion object {
        const val LIKE = "LIKE"
        private const val POOL_MULTIPLIER = 5
        private const val JITTER_RANGE = 15.0
    }

    fun areFriends(user1: UUID, user2: UUID): Boolean {
        val (first, second) = if (user1 < user2) user1 to user2 else user2 to user1
        return matchRepository.existsByUserId1AndUserId2(first, second)
    }

    fun getNextCandidates(userId: UUID, batchSize: Int): MatchCandidateBatchResponse {
        val filters = matchFilterRepository.findById(userId).orElse(MatchFilter(userId = userId))

        val poolSize = batchSize * POOL_MULTIPLIER
        val candidates = userProfilesRepository.findCandidates(
            userId = userId,
            gender = filters.gender,
            city = filters.city,
            ageFrom = filters.ageFrom,
            ageTo = filters.ageTo,
            fandomId = filters.fandomId,
            fandomCategory = filters.fandomCategory,
            pageable = PageRequest.of(0, poolSize)
        )

        val random = Random()

        val suggested = candidates
            .map { candidate ->
                val candidateFandoms = fandomService.getFandoms(candidate.userId)
                val compatibility = calculateCompatibility(userId, candidate.userId)
                val jitteredScore = compatibility + random.nextDouble(-JITTER_RANGE, JITTER_RANGE)
                Triple(candidate.toMatchCandidateResponse(compatibility, candidateFandoms), jitteredScore, compatibility)
            }
            .sortedByDescending { it.second }
            .take(batchSize)
            .map { it.first }

        if (suggested.isNotEmpty()) {
            matchPendingRepository.saveAll(suggested.map { it.toMatchPending(userId) })
        }

        return MatchCandidateBatchResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = MatchCandidateBatchData(suggested)
        )
    }


    @Transactional
    fun react(userId: UUID, targetUsername: String, action: String): MatchActionResponse {
        val targetProfile = userProfilesRepository.findByUsername(targetUsername)
            .orElseThrow { UserNotFoundException(targetUsername) }
        val targetUserId = targetProfile.userId

        val existingAction = matchActionRepository.findByUserIdAndTargetUserId(userId, targetUserId)
        if (existingAction != null) {
            throw AlreadyReactedException(targetUsername)
        }

        val matchAction = MatchAction(
            userId = userId,
            targetUserId = targetUserId,
            action = action
        )
        matchActionRepository.save(matchAction)
        matchPendingRepository.deleteByUserIdAndSuggestedUserId(userId, targetUserId)

        val oppositeAction = hasTargetUserLikedCurrentUser(userId, targetUserId)
        val isMutual = oppositeAction?.action == LIKE && action == LIKE

        val result = if (isMutual) {
            val (first, second) = if (userId < targetUserId) userId to targetUserId else targetUserId to userId
            val match = Match(userId1 = first, userId2 = second)
            matchRepository.save(match)
            matchEventProducer.sendMatchEvent(match.id!!, first, second)

            MatchActionResult(
                status = MatchActionResult.Status.MATCH,
            )
        } else {
            MatchActionResult(
                status = if (action == LIKE) MatchActionResult.Status.LIKED else MatchActionResult.Status.DISLIKED,
            )
        }

        return MatchActionResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = result
        )
    }

    fun hasTargetUserLikedCurrentUser(currentUserId: UUID, targetUserId: UUID) =
        matchActionRepository.findByUserIdAndTargetUserId(targetUserId, currentUserId)

    private fun calculateCompatibility(currentUserId: UUID, candidateId: UUID): Double {
        val currentUserFandoms = fandomService.getFandoms(currentUserId).toSet()
        val candidateFandoms = fandomService.getFandoms(candidateId).toSet()

        val common = currentUserFandoms.intersect(candidateFandoms).size
        return if (currentUserFandoms.isNotEmpty() && candidateFandoms.isNotEmpty()) {
            (common * 100).toDouble() / min(currentUserFandoms.size, candidateFandoms.size)
        } else 0.0
    }

    fun setFilter(userId: UUID, request: MatchFilterRequest): MatchFilterResponse {
        val filter = MatchFilter(
            userId = userId,
            gender = request.gender,
            city = request.city,
            ageFrom = request.ageFrom,
            ageTo = request.ageTo,
            fandomCategory = request.fandomCategory?.let { UUID.fromString(it) },
            fandomId = request.fandomId?.let { UUID.fromString(it) }
        )
        matchFilterRepository.save(filter)

        return MatchFilterResponse(
            status = ResponseStatus.SUCCESS,
        )
    }
}