package service

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import com.fandomatch.notifications.PushNotificationService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.adapter.UsersNotificationAdapter
import org.example.exceptions.AlreadyReactedException
import org.example.exceptions.UserNotFoundException
import org.example.models.db_models.Match
import org.example.models.db_models.MatchPending
import org.example.repository.*
import org.example.service.FandomService
import org.example.service.MatchesService
import org.example.stream.out.LikeEventProducer
import org.example.stream.out.MatchEventProducer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.*
import utils.Constants.CANDIDATE_USER_ID
import utils.Constants.TARGET_USER_ID
import utils.Constants.USER_ID
import java.util.*

@ExtendWith(MockKExtension::class)
class MatchesServiceTest {

    @MockK
    lateinit var matchRepository: MatchRepository

    @MockK
    lateinit var userProfilesRepository: UserProfileRepository

    @MockK
    lateinit var matchFilterRepository: MatchFilterRepository

    @MockK
    lateinit var fandomService: FandomService

    @MockK
    lateinit var fandomRepository: FandomRepository

    @MockK
    lateinit var fandomCategoryRepository: FandomCategoryRepository

    @MockK
    lateinit var matchPendingRepository: MatchPendingRepository

    @MockK
    lateinit var matchEventProducer: MatchEventProducer

    @MockK
    lateinit var matchActionRepository: MatchActionRepository

    @MockK(relaxed = true)
    lateinit var mediaService: MediaService

    @MockK
    lateinit var likeEventProducer: LikeEventProducer

    @MockK
    lateinit var usersNotificationAdapter: UsersNotificationAdapter

    @MockK(relaxed = true)
    lateinit var pushNotificationService: PushNotificationService

    @InjectMockKs
    private lateinit var matchesService: MatchesService

    // --- areFriends ---

    @Test
    fun `areFriends should return true when match exists`() {
        every { matchRepository.existsByUserId1AndUserId2(USER_ID, TARGET_USER_ID) } returns true

        val result = matchesService.areFriends(USER_ID, TARGET_USER_ID)

        assertTrue(result)
    }

    @Test
    fun `areFriends should order UUIDs so smaller is first`() {
        every { matchRepository.existsByUserId1AndUserId2(USER_ID, TARGET_USER_ID) } returns false

        val result = matchesService.areFriends(TARGET_USER_ID, USER_ID)

        assertFalse(result)
        verify { matchRepository.existsByUserId1AndUserId2(USER_ID, TARGET_USER_ID) }
    }

    // --- react ---

    @Test
    fun `react with LIKE should return LIKED when no opposite action`() {
        every { userProfilesRepository.existsById(TARGET_USER_ID) } returns true
        every { matchActionRepository.findByUserIdAndTargetUserId(USER_ID, TARGET_USER_ID) } returns null
        every { matchActionRepository.save(any()) } answers { firstArg() }
        every { matchPendingRepository.deleteByUserIdAndSuggestedUserId(USER_ID, TARGET_USER_ID) } just runs
        every { matchActionRepository.findByUserIdAndTargetUserId(TARGET_USER_ID, USER_ID) } returns null
        every { likeEventProducer.send(USER_ID, TARGET_USER_ID) } just runs

        val result = matchesService.react(USER_ID, TARGET_USER_ID, "LIKE")

        assertEquals(ResponseStatus.SUCCESS, result.status)
        assertEquals(MatchActionResult.Status.LIKED, result.successResponse!!.status)
        verify(exactly = 1) { matchActionRepository.save(match { it.action == "LIKE" }) }
    }

    @Test
    fun `react with DISLIKE should return DISLIKED`() {
        every { userProfilesRepository.existsById(TARGET_USER_ID) } returns true
        every { matchActionRepository.findByUserIdAndTargetUserId(USER_ID, TARGET_USER_ID) } returns null
        every { matchActionRepository.save(any()) } answers { firstArg() }
        every { matchPendingRepository.deleteByUserIdAndSuggestedUserId(USER_ID, TARGET_USER_ID) } just runs
        every { matchActionRepository.findByUserIdAndTargetUserId(TARGET_USER_ID, USER_ID) } returns null

        val result = matchesService.react(USER_ID, TARGET_USER_ID, "DISLIKE")

        assertEquals(ResponseStatus.SUCCESS, result.status)
        assertEquals(MatchActionResult.Status.DISLIKED, result.successResponse!!.status)
        // no like event should be produced for DISLIKE
        verify(exactly = 0) { likeEventProducer.send(any(), any()) }
    }

    @Test
    fun `react with mutual LIKE should return MATCH and save match`() {
        val oppositeAction = createMatchAction(userId = TARGET_USER_ID, targetUserId = USER_ID, action = "LIKE")

        every { userProfilesRepository.existsById(TARGET_USER_ID) } returns true
        every { matchActionRepository.findByUserIdAndTargetUserId(USER_ID, TARGET_USER_ID) } returns null
        every { matchActionRepository.save(any()) } answers { firstArg() }
        every { matchPendingRepository.deleteByUserIdAndSuggestedUserId(USER_ID, TARGET_USER_ID) } just runs
        every { matchActionRepository.findByUserIdAndTargetUserId(TARGET_USER_ID, USER_ID) } returns oppositeAction
        every { matchRepository.save(any()) } answers {
            val arg = firstArg<Match>()
            arg.copy(id = UUID.randomUUID())
        }
        every { matchEventProducer.sendMatchEvent(any(), any(), any()) } just runs
        every { userProfilesRepository.findById(USER_ID) } returns Optional.of(createUserProfile(userId = USER_ID))
        every { userProfilesRepository.findById(TARGET_USER_ID) } returns Optional.of(createUserProfile(userId = TARGET_USER_ID))
        every { usersNotificationAdapter.getFcmToken(any()) } returns null

        val result = matchesService.react(USER_ID, TARGET_USER_ID, "LIKE")

        assertEquals(ResponseStatus.SUCCESS, result.status)
        assertEquals(MatchActionResult.Status.MATCH, result.successResponse!!.status)
        verify(exactly = 1) { matchRepository.save(match { it.userId1 == USER_ID && it.userId2 == TARGET_USER_ID }) }
        verify(exactly = 1) { matchEventProducer.sendMatchEvent(any(), USER_ID, TARGET_USER_ID) }
    }

    @Test
    fun `react should throw UserNotFoundException when target user does not exist`() {
        every { userProfilesRepository.existsById(TARGET_USER_ID) } returns false

        assertThrows(UserNotFoundException::class.java) {
            matchesService.react(USER_ID, TARGET_USER_ID, "LIKE")
        }
    }

    @Test
    fun `react should throw AlreadyReactedException when action already exists`() {
        val existingAction = createMatchAction(userId = USER_ID, targetUserId = TARGET_USER_ID)

        every { userProfilesRepository.existsById(TARGET_USER_ID) } returns true
        every { matchActionRepository.findByUserIdAndTargetUserId(USER_ID, TARGET_USER_ID) } returns existingAction

        assertThrows(AlreadyReactedException::class.java) {
            matchesService.react(USER_ID, TARGET_USER_ID, "LIKE")
        }
    }

    // --- getNextCandidates ---

    @Test
    fun `getNextCandidates should return empty list when no candidates`() {
        val filter = createMatchFilter(userId = USER_ID)

        every { matchPendingRepository.findAllByUserIdOrderByCreatedAtAsc(USER_ID) } returns emptyList()
        every { matchFilterRepository.findById(USER_ID) } returns Optional.of(filter)
        every { fandomService.getFandoms(USER_ID) } returns emptyList()
        every { userProfilesRepository.findCandidates(any(), any(), any(), any(), any(), any(), any(), any()) } returns emptyList()

        val result = matchesService.getNextCandidates(USER_ID, 5)

        assertEquals(ResponseStatus.SUCCESS, result.status)
        assertTrue(result.successResponse!!.candidates.isEmpty())
        verify(exactly = 0) { matchPendingRepository.saveAll(any<List<MatchPending>>()) }
    }

    @Test
    fun `getNextCandidates should use default filter when none exists`() {
        every { matchPendingRepository.findAllByUserIdOrderByCreatedAtAsc(USER_ID) } returns emptyList()
        every { matchFilterRepository.findById(USER_ID) } returns Optional.empty()
        every { fandomService.getFandoms(USER_ID) } returns emptyList()
        every { userProfilesRepository.findCandidates(any(), any(), any(), any(), any(), any(), any(), any()) } returns emptyList()

        val result = matchesService.getNextCandidates(USER_ID, 5)

        assertEquals(ResponseStatus.SUCCESS, result.status)
    }

    @Test
    fun `getNextCandidates should return candidates with compatibility`() {
        val candidate = createUserProfile(userId = CANDIDATE_USER_ID, username = "candidate")
        val filter = createMatchFilter(userId = USER_ID)
        val sharedFandom = createFandom()
        val userFandoms = listOf(sharedFandom)
        val candidateFandoms = listOf(sharedFandom)

        every { matchPendingRepository.findAllByUserIdOrderByCreatedAtAsc(USER_ID) } returns emptyList()
        every { matchFilterRepository.findById(USER_ID) } returns Optional.of(filter)
        every { fandomService.getFandoms(USER_ID) } returns userFandoms
        every { userProfilesRepository.findCandidates(any(), any(), any(), any(), any(), any(), any(), any()) } returns listOf(candidate)
        every { fandomService.getFandoms(CANDIDATE_USER_ID) } returns candidateFandoms
        every { matchPendingRepository.insertIgnore(any(), any()) } just runs

        val result = matchesService.getNextCandidates(USER_ID, 5)

        assertEquals(ResponseStatus.SUCCESS, result.status)
        assertEquals(1, result.successResponse!!.candidates.size)
        assertEquals(CANDIDATE_USER_ID.toString(), result.successResponse!!.candidates[0].uuid)
        // Current user fandoms fetched once, candidate fandoms fetched once per candidate
        verify(exactly = 1) { fandomService.getFandoms(USER_ID) }
        verify(exactly = 1) { fandomService.getFandoms(CANDIDATE_USER_ID) }
        verify(exactly = 1) { matchPendingRepository.insertIgnore(USER_ID, CANDIDATE_USER_ID) }
    }

    // --- setFilter ---

    @Test
    fun `setFilter should save filter and return success`() {
        val request = MatchFilterRequest(
            filters = MatchFilter(
                gender = listOf(Gender.MALE),
                ageFrom = 18,
                ageTo = 30
            )
        )

        every { matchFilterRepository.save(any()) } answers { firstArg() }
        every { matchPendingRepository.deleteAllByUserId(USER_ID) } just runs

        val result = matchesService.setFilter(USER_ID, request)

        assertEquals(ResponseStatus.SUCCESS, result.status)
        verify(exactly = 1) {
            matchFilterRepository.save(match {
                it.userId == USER_ID && it.gender == listOf("MALE") && it.ageFrom == 18 && it.ageTo == 30
            })
        }
        verify(exactly = 1) { matchPendingRepository.deleteAllByUserId(USER_ID) }
    }

    @Test
    fun `setFilter should handle null filter values`() {
        val request = MatchFilterRequest(
            filters = MatchFilter()
        )

        every { matchFilterRepository.save(any()) } answers { firstArg() }
        every { matchPendingRepository.deleteAllByUserId(USER_ID) } just runs

        val result = matchesService.setFilter(USER_ID, request)

        assertEquals(ResponseStatus.SUCCESS, result.status)
        verify(exactly = 1) {
            matchFilterRepository.save(match {
                it.userId == USER_ID && it.gender == null && it.ageFrom == null && it.ageTo == null
            })
        }
        verify(exactly = 1) { matchPendingRepository.deleteAllByUserId(USER_ID) }
    }
}
