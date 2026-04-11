package service

import com.fandomatch.core.model.*
import com.fandomatch.media.MediaService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.client.UsersAdapter
import org.example.exceptions.UserNotFoundException
import org.example.models.ProfileData
import org.example.repository.UserProfileRepository
import org.example.service.FandomService
import org.example.service.MatchesService
import org.example.service.profile.ConstructOtherProfile
import org.example.service.profile.ConstructOwnProfile
import org.example.service.profile.ConstructFriendProfile
import org.example.service.profile.ProfileStrategyFactory
import org.example.service.profile.ProfilesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.Constants.TARGET_USER_ID
import utils.Constants.TARGET_USERNAME
import utils.Constants.USER_ID
import utils.Constants.USERNAME
import utils.*
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@ExtendWith(MockKExtension::class)
class ProfilesServiceTest {

    @MockK
    lateinit var userProfileRepository: UserProfileRepository

    @MockK
    lateinit var fandomService: FandomService

    @MockK
    lateinit var usersAdapter: UsersAdapter

    @MockK
    lateinit var strategyFactory: ProfileStrategyFactory

    @MockK
    lateinit var matchesService: MatchesService

    @MockK(relaxed = true)
    lateinit var mediaService: MediaService

    @InjectMockKs
    private lateinit var profilesService: ProfilesService

    @Test
    fun `getProfile should return OWN profile when viewing own profile`() {
        val userProfile = createUserProfile(userId = USER_ID, username = USERNAME)
        val creds = createUserCredentials()
        val fandoms = listOf(createFandom())

        every { userProfileRepository.findByUsername(USERNAME) } returns Optional.of(userProfile)
        every { usersAdapter.getUserCredentialsByUuid(USER_ID) } returns creds
        every { fandomService.getFandoms(USER_ID) } returns fandoms
        every { strategyFactory.getStrategy(ProfileType.OWN, any()) } answers {
            ConstructOwnProfile(secondArg(), mediaService)
        }

        val result = profilesService.getProfile(USER_ID, USERNAME)

        assertEquals(ResponseStatus.SUCCESS, result.status)
        verify(exactly = 1) { usersAdapter.getUserCredentialsByUuid(USER_ID) }
        verify(exactly = 1) { fandomService.getFandoms(USER_ID) }
    }

    @Test
    fun `getProfile should return FRIEND profile when users are friends`() {
        val targetProfile = createUserProfile(userId = TARGET_USER_ID, username = TARGET_USERNAME)
        val fandoms = listOf(createFandom())

        every { userProfileRepository.findByUsername(TARGET_USERNAME) } returns Optional.of(targetProfile)
        every { fandomService.getFandoms(TARGET_USER_ID) } returns fandoms
        every { matchesService.areFriends(USER_ID, TARGET_USER_ID) } returns true
        every { strategyFactory.getStrategy(ProfileType.FRIEND, any()) } answers {
            ConstructFriendProfile(secondArg(), mediaService)
        }

        val result = profilesService.getProfile(USER_ID, TARGET_USERNAME)

        assertEquals(ResponseStatus.SUCCESS, result.status)
        verify(exactly = 1) { matchesService.areFriends(USER_ID, TARGET_USER_ID) }
        verify(exactly = 0) { usersAdapter.getUserCredentialsByUuid(any()) }
    }

    @Test
    fun `getProfile should return OTHER profile when users are not friends`() {
        val targetProfile = createUserProfile(userId = TARGET_USER_ID, username = TARGET_USERNAME)
        val fandoms = listOf(createFandom())

        every { userProfileRepository.findByUsername(TARGET_USERNAME) } returns Optional.of(targetProfile)
        every { fandomService.getFandoms(TARGET_USER_ID) } returns fandoms
        every { matchesService.areFriends(USER_ID, TARGET_USER_ID) } returns false
        every { strategyFactory.getStrategy(ProfileType.OTHER, any()) } answers {
            ConstructOtherProfile(secondArg(), mediaService)
        }

        val result = profilesService.getProfile(USER_ID, TARGET_USERNAME)

        assertEquals(ResponseStatus.SUCCESS, result.status)
        verify(exactly = 1) { matchesService.areFriends(USER_ID, TARGET_USER_ID) }
        verify(exactly = 0) { usersAdapter.getUserCredentialsByUuid(any()) }
    }

    @Test
    fun `getProfile should throw UserNotFoundException when user does not exist`() {
        every { userProfileRepository.findByUsername("nonexistent") } returns Optional.empty()

        assertThrows(UserNotFoundException::class.java) {
            profilesService.getProfile(USER_ID, "nonexistent")
        }
    }

    @Test
    fun `editProfile should update profile and return OWN profile`() {
        val existingProfile = createUserProfile(userId = USER_ID)
        val creds = createUserCredentials()
        val fandoms = listOf(createFandom())

        val request = EditUserProfileRequest(
            name = "Updated Name",
            bio = "Updated bio"
        )

        every { userProfileRepository.findById(USER_ID) } returns Optional.of(existingProfile)
        every { userProfileRepository.save(any()) } answers { firstArg() }
        every { usersAdapter.getUserCredentialsByUuid(USER_ID) } returns creds
        every { fandomService.getFandoms(USER_ID) } returns fandoms
        every { strategyFactory.getStrategy(ProfileType.OWN, any()) } answers {
            ConstructOwnProfile(secondArg(), mediaService)
        }

        val result = profilesService.editProfile(USER_ID, request)

        assertEquals(ResponseStatus.SUCCESS, result.status)
        verify(exactly = 1) { userProfileRepository.save(match { it.name == "Updated Name" && it.bio == "Updated bio" }) }
    }

    @Test
    fun `editProfile should throw UserNotFoundException when profile does not exist`() {
        val request = EditUserProfileRequest(name = "New Name")

        every { userProfileRepository.findById(USER_ID) } returns Optional.empty()

        assertThrows(UserNotFoundException::class.java) {
            profilesService.editProfile(USER_ID, request)
        }
    }

    @Test
    fun `createProfile should save new user profile`() {
        val event = UserChangedEvent(
            uid = USER_ID.toString(),
            username = USERNAME,
            name = "New User",
            birthDate = 946684800L,
            createdAt = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            status = UserStatus.ACTIVE,
            eventType = EventType.CREATED
        )

        every { userProfileRepository.existsById(USER_ID) } returns false
        every { userProfileRepository.save(any()) } answers { firstArg() }

        profilesService.createProfile(event)

        verify(exactly = 1) { userProfileRepository.save(match { it.userId == USER_ID && it.username == USERNAME }) }
    }

    @Test
    fun `createProfile should skip when profile already exists`() {
        val event = UserChangedEvent(
            uid = USER_ID.toString(),
            username = USERNAME,
            name = "New User",
            birthDate = 946684800L,
            createdAt = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            status = UserStatus.ACTIVE,
            eventType = EventType.CREATED
        )

        every { userProfileRepository.existsById(USER_ID) } returns true

        profilesService.createProfile(event)

        verify(exactly = 0) { userProfileRepository.save(any()) }
    }
}
