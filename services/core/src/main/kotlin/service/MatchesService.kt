package org.example.service

import com.fandomatch.core.model.MatchCandidateBatchData
import com.fandomatch.core.model.MatchCandidateBatchResponse
import com.fandomatch.core.model.ResponseStatus
import org.example.models.db_models.MatchFilter
import org.example.repository.MatchFilterRepository
import org.example.repository.MatchPendingRepository
import org.example.repository.MatchRepository
import org.example.repository.UserProfileRepository
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
    private val matchPendingRepository: MatchPendingRepository
) {

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

    private fun calculateCompatibility(currentUserId: UUID, candidateId: UUID): Double {
        val currentUserFandoms = fandomService.getFandoms(currentUserId).toSet()
        val candidateFandoms = fandomService.getFandoms(candidateId).toSet()

        val common = currentUserFandoms.intersect(candidateFandoms).size
        return if (currentUserFandoms.isNotEmpty() && candidateFandoms.isNotEmpty()) {
            (common * 100).toDouble() / (min(candidateFandoms.size, candidateFandoms.size))
        } else 0.0
    }


}