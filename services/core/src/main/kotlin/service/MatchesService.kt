package org.example.service

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import com.fandomatch.notifications.PushNotificationService
import jakarta.transaction.Transactional
import org.example.adapter.UsersNotificationAdapter
import org.example.exceptions.AlreadyReactedException
import org.example.exceptions.FandomCategoryNotFoundException
import org.example.exceptions.UserNotFoundException
import org.example.models.db_models.Match
import org.example.models.db_models.MatchAction
import org.example.models.db_models.MatchFilter
import org.example.repository.*
import org.example.stream.out.LikeEventProducer
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
    private val fandomRepository: FandomRepository,
    private val fandomCategoryRepository: FandomCategoryRepository,
    private val matchPendingRepository: MatchPendingRepository,
    private val matchEventProducer: MatchEventProducer,
    private val matchActionRepository: MatchActionRepository,
    private val mediaService: MediaService,
    private val likeEventProducer: LikeEventProducer,
    private val usersNotificationAdapter: UsersNotificationAdapter,
    private val pushNotificationService: PushNotificationService
) {
    companion object {
        const val LIKE = "LIKE"
        private const val POOL_MULTIPLIER = 5
        private const val JITTER_RANGE = 15.0
    }

    fun areFriends(user1: UUID, user2: UUID): Boolean {
        val (first, second) = if (user1.toString() < user2.toString()) user1 to user2 else user2 to user1
        return matchRepository.existsByUserId1AndUserId2(first, second)
    }

    fun getPendingRequestUserIds(userId: UUID): List<UUID> {
        return matchActionRepository.findAllByUserIdAndAction(userId, LIKE)
            .filter { matchActionRepository.findByUserIdAndTargetUserId(it.targetUserId, userId) == null }
            .filter { !areFriends(userId, it.targetUserId) }
            .map { it.targetUserId }
    }

    fun getFriendIds(userId: UUID): List<UUID> {
        return matchRepository.getAllUserMatches(userId).map { match ->
            if (match.userId1 == userId) match.userId2 else match.userId1
        }
    }

    @jakarta.transaction.Transactional
    fun getNextCandidates(userId: UUID, batchSize: Int): MatchCandidateBatchResponse {
        val currentUserFandoms = fandomService.getFandoms(userId)

        val pending = matchPendingRepository.findAllByUserIdOrderByCreatedAtAsc(userId)
        if (pending.isNotEmpty()) {
            val toServe = pending.take(batchSize)
            val pendingCandidates = toServe
                .mapNotNull { userProfilesRepository.findById(it.suggestedUserId).orElse(null) }
                .filter { it.birthDate != null && it.gender != null && it.name != null }
                .map { profile ->
                    val candidateFandoms = fandomService.getFandoms(profile.userId)
                    val compatibility = calculateCompatibility(currentUserFandoms, candidateFandoms)
                    profile.toMatchCandidateResponse(compatibility, candidateFandoms, mediaService)
                }

            matchPendingRepository.deleteAll(toServe)

            if (pendingCandidates.isNotEmpty()) {
                return MatchCandidateBatchResponse(
                    status = ResponseStatus.SUCCESS,
                    successResponse = MatchCandidateBatchData(pendingCandidates)
                )
            }
        }

        val filters = matchFilterRepository.findById(userId).orElse(MatchFilter(userId = userId))!!

        val userCity = if (filters.onlyInUserCity == true) {
            userProfilesRepository.findById(userId).orElse(null)?.city
        } else null

        val poolSize = batchSize * POOL_MULTIPLIER
        val candidates = userProfilesRepository.findCandidates(
            userId = userId,
            gender = filters.gender?.firstOrNull(),
            city = userCity,
            ageFrom = filters.ageFrom,
            ageTo = filters.ageTo,
            fandomId = filters.fandomIds?.firstOrNull()?.let { UUID.fromString(it) },
            fandomCategory = filters.fandomCategory?.firstOrNull()
                ?.let { name -> fandomCategoryRepository.findByName(name)?.id },
            pageable = PageRequest.of(0, poolSize)
        )

        val random = Random()

        val suggested = candidates
            .asSequence()
            .filter { it.birthDate != null && it.gender != null && it.name != null }
            .map { candidate ->
                val candidateFandoms = fandomService.getFandoms(candidate.userId)
                val compatibility = calculateCompatibility(currentUserFandoms, candidateFandoms)
                val jitteredScore = compatibility + random.nextDouble(-JITTER_RANGE, JITTER_RANGE)
                Triple(candidate.toMatchCandidateResponse(compatibility, candidateFandoms, mediaService), jitteredScore, compatibility)
            }
            .sortedByDescending { it.second }
            .take(batchSize)
            .map { it.first }
            .toList()

        if (suggested.isNotEmpty()) {
            suggested.forEach { matchPendingRepository.insertIgnore(userId, UUID.fromString(it.uuid)) }
        }

        return MatchCandidateBatchResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = MatchCandidateBatchData(suggested)
        )
    }

    @Transactional
    fun react(userId: UUID, targetUuid: UUID, action: String): MatchActionResponse {
        if (!userProfilesRepository.existsById(targetUuid)) {
            throw UserNotFoundException(targetUuid.toString())
        }

        val existingAction = matchActionRepository.findByUserIdAndTargetUserId(userId, targetUuid)
        if (existingAction != null) {
            throw AlreadyReactedException(targetUuid.toString())
        }

        val matchAction = MatchAction(
            userId = userId,
            targetUserId = targetUuid,
            action = action
        )
        matchActionRepository.save(matchAction)
        matchPendingRepository.deleteByUserIdAndSuggestedUserId(userId, targetUuid)

        val oppositeAction = hasTargetUserLikedCurrentUser(userId, targetUuid)
        val isMutual = oppositeAction?.action == LIKE && action == LIKE

        val result = if (isMutual) {
            val (first, second) = if (userId.toString() < targetUuid.toString()) userId to targetUuid else targetUuid to userId
            val match = Match(userId1 = first, userId2 = second)
            val savedMatch = matchRepository.save(match)
            matchEventProducer.sendMatchEvent(savedMatch.id!!, first, second)

            val userProfile = userProfilesRepository.findById(userId).orElse(null)
            val targetProfile = userProfilesRepository.findById(targetUuid).orElse(null)
            usersNotificationAdapter.getFcmToken(userId)?.let { token ->
                pushNotificationService.sendDataMessage(
                    token, mapOf(
                        "type" to "match",
                        "userId" to targetUuid.toString(),
                        "name" to (targetProfile?.name ?: targetProfile?.username ?: targetUuid.toString())
                    )
                )
            }
            usersNotificationAdapter.getFcmToken(targetUuid)?.let { token ->
                pushNotificationService.sendDataMessage(
                    token, mapOf(
                        "type" to "match",
                        "userId" to userId.toString(),
                        "name" to (userProfile?.name ?: userProfile?.username ?: userId.toString())
                    )
                )
            }

            MatchActionResult(status = MatchActionResult.Status.MATCH)
        } else {
            if (action == LIKE) {
                likeEventProducer.send(userId, targetUuid)
            }
            MatchActionResult(
                status = if (action == LIKE) MatchActionResult.Status.LIKED else MatchActionResult.Status.DISLIKED
            )
        }

        return MatchActionResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = result
        )
    }

    fun hasTargetUserLikedCurrentUser(currentUserId: UUID, targetUserId: UUID) =
        matchActionRepository.findByUserIdAndTargetUserId(targetUserId, currentUserId)

    @Transactional
    fun setFilter(userId: UUID, request: MatchFilterRequest): MatchFilterResponse {
        val filter = MatchFilter(
            userId = userId,
            gender = request.filters.gender?.map { it.name }?.takeIf { it.isNotEmpty() },
            onlyInUserCity = request.filters.onlyInUserCity,
            ageFrom = request.filters.ageFrom,
            ageTo = request.filters.ageTo,
            fandomCategory = request.filters.fandomCategory?.map { it.name }?.takeIf { it.isNotEmpty() },
            fandomIds = request.filters.fandomId?.map { it.id }?.takeIf { it.isNotEmpty() }
        )
        matchFilterRepository.save(filter)
        matchPendingRepository.deleteAllByUserId(userId)

        return MatchFilterResponse(status = ResponseStatus.SUCCESS)
    }

    fun getCurrentFilters(userId: UUID): CurrentFiltersResponse {
        val filter = matchFilterRepository.findById(userId).orElse(null)
            ?: return CurrentFiltersResponse(
                status = ResponseStatus.SUCCESS,
                successResponse = MatchFilter()
            )

        val fandoms = filter.fandomIds?.mapNotNull { id ->
            val fandom = fandomRepository.findById(UUID.fromString(id)).orElse(null) ?: return@mapNotNull null
            val category = fandomCategoryRepository.findById(fandom.categoryId)
                .orElseThrow { FandomCategoryNotFoundException(fandom.categoryId.toString()) }
            Fandom(
                id = fandom.id!!.toString(),
                name = fandom.name,
                category = category.let { FandomCategory.valueOf(it.name) }
            )
        }

        return CurrentFiltersResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = MatchFilter(
                gender = filter.gender?.mapNotNull { name ->
                    runCatching { Gender.valueOf(name) }.getOrNull()
                },
                ageFrom = filter.ageFrom,
                ageTo = filter.ageTo,
                onlyInUserCity = filter.onlyInUserCity,
                fandomCategory = filter.fandomCategory?.mapNotNull { name ->
                    runCatching { FandomCategory.valueOf(name) }.getOrNull()
                },
                fandomId = fandoms
            )
        )
    }

    private fun calculateCompatibility(currentUserFandoms: List<Fandom>, candidateFandoms: List<Fandom>): Double {
        val currentSet = currentUserFandoms.toSet()
        val candidateSet = candidateFandoms.toSet()

        val common = currentSet.intersect(candidateSet).size
        return if (currentSet.isNotEmpty() && candidateSet.isNotEmpty()) {
            (common * 100).toDouble() / min(currentSet.size, candidateSet.size)
        } else 0.0
    }
}
