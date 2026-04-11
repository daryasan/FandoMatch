package controller

import com.fandomatch.core.model.*
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.controller.ProfilesController
import org.example.exceptions.UserNotFoundException
import org.example.models.UserTokenData
import org.example.service.TokenParserService
import org.example.service.profile.ProfilesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpStatus
import utils.Constants.USER_ID
import utils.Constants.USERNAME
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class ProfilesControllerTest {

    @MockK
    lateinit var profilesService: ProfilesService

    @MockK
    lateinit var tokenParserService: TokenParserService

    @InjectMockKs
    private lateinit var profilesController: ProfilesController

    private val token = "Bearer test-token"
    private val tokenData = UserTokenData(userId = USER_ID, username = USERNAME)

    @Test
    fun `getProfile should return SUCCESS when service succeeds`() {
        val request = UserProfileRequest(username = USERNAME)
        val profileResponse = UserProfileResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = PublicUserProfileResponse(
                profileType = ProfileType.OTHER,
                uid = USER_ID.toString(),
                name = "Test"
            )
        )

        every { tokenParserService.parse(token) } returns tokenData
        every { profilesService.getProfile(USER_ID, USERNAME) } returns profileResponse

        val result = profilesController.getProfile(token, request)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(ResponseStatus.SUCCESS, result.body!!.status)
        verify(exactly = 1) { profilesService.getProfile(USER_ID, USERNAME) }
    }

    @Test
    fun `getProfile should return ERROR when user not found`() {
        val request = UserProfileRequest(username = "nonexistent")

        every { tokenParserService.parse(token) } returns tokenData
        every { profilesService.getProfile(USER_ID, "nonexistent") } throws
                UserNotFoundException("nonexistent")

        val result = profilesController.getProfile(token, request)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(ResponseStatus.ERROR, result.body!!.status)
        assertNotNull(result.body!!.errorResponse)
        assertEquals("USER_NOT_FOUND", result.body!!.errorResponse!!.errorCode)
    }

    @Test
    fun `editProfile should return SUCCESS when service succeeds`() {
        val request = EditUserProfileRequest(name = "Updated")
        val fullProfile = FullUserProfileResponse(
            profileType = ProfileType.OWN,
            uid = USER_ID.toString(),
            username = USERNAME,
            name = "Updated",
            birthDate = 946684800L,
            age = 0L,
            status = "ACTIVE",
            createdAt = OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)
        )
        val editResponse = EditUserProfileResponse(
            status = ResponseStatus.SUCCESS,
            successResponse = fullProfile
        )

        every { tokenParserService.parse(token) } returns tokenData
        every { profilesService.editProfile(USER_ID, request) } returns editResponse

        val result = profilesController.editProfile(token, request)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(ResponseStatus.SUCCESS, result.body!!.status)
        verify(exactly = 1) { profilesService.editProfile(USER_ID, request) }
    }

    @Test
    fun `editProfile should return ERROR when user not found`() {
        val request = EditUserProfileRequest(name = "Updated")

        every { tokenParserService.parse(token) } returns tokenData
        every { profilesService.editProfile(USER_ID, request) } throws
                UserNotFoundException(USER_ID.toString())

        val result = profilesController.editProfile(token, request)

        assertEquals(HttpStatus.OK, result.statusCode)
        assertEquals(ResponseStatus.ERROR, result.body!!.status)
        assertNotNull(result.body!!.errorResponse)
        assertEquals("USER_NOT_FOUND", result.body!!.errorResponse!!.errorCode)
    }
}
