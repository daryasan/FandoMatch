package service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import org.example.model.AuthTokens
import org.example.model.db_models.UserCredential
import org.example.model.db_models.enums.CredentialType
import org.example.service.AuthService
import org.example.service.TokenService
import org.example.service.UserCredentialsService
import org.example.service.UserService
import org.example.service.VerificationCodeService
import org.example.service.validation.UserValidator
import org.example.stream.out.UserEventsSender
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import com.fandomatch.users.model.Gender
import utils.Constants.BIRTH_DATE
import utils.Constants.EMAIL
import utils.Constants.GENDER
import utils.Constants.NAME
import utils.Constants.PASSWORD
import utils.Constants.USERNAME
import utils.createUser
import java.util.UUID

@ExtendWith(MockKExtension::class)
class AuthServiceTest {

    @MockK
    lateinit var userService: UserService

    @MockK
    lateinit var tokenService: TokenService

    @MockK
    lateinit var userCredentialsService: UserCredentialsService

    @MockK
    lateinit var userValidator: UserValidator

    @MockK
    lateinit var userEventsSender: UserEventsSender

    @MockK
    lateinit var verificationCodeService: VerificationCodeService

    @InjectMockKs
    private lateinit var authService: AuthService

    @Test
    fun `register should create user, save credentials, send event, generate tokens`() {
        // given
        val user = createUser(EMAIL, USERNAME)
        val expectedTokens = AuthTokens(
            accessToken = "access-123",
            refreshToken = "refresh-123",
            uuid = UUID.randomUUID()
        )

        every { userService.createUser(EMAIL, USERNAME) } returns user
        every { userCredentialsService.createCredentials(user, PASSWORD) } returns UserCredential(
            user = user,
            credentialType = CredentialType.PASSWORD
        )
        every { userEventsSender.sendUserCreatedEvent(user, any(), NAME, BIRTH_DATE, GENDER, null) } just runs
        every { tokenService.generateAndSaveTokens(user) } returns expectedTokens

        // when
        val result = authService.register(EMAIL, USERNAME, PASSWORD, BIRTH_DATE, NAME, Gender.valueOf(GENDER))

        // then
        verify(exactly = 1) { userService.createUser(EMAIL, USERNAME) }
        verify(exactly = 1) { userCredentialsService.createCredentials(user, PASSWORD) }
        verify(exactly = 1) { userEventsSender.sendUserCreatedEvent(user, any(), NAME, BIRTH_DATE, GENDER, null) }
        verify(exactly = 1) { tokenService.generateAndSaveTokens(user) }

        assertEquals(expectedTokens, result)
    }

    @Test
    fun `login should find user, validate, verify credentials and generate tokens`() {
        // given
        val user = createUser(EMAIL, USERNAME)
        val expectedTokens = AuthTokens(
            accessToken = "access-login",
            refreshToken = "refresh-login",
            uuid = UUID.randomUUID()
        )

        every { userService.findUser(EMAIL) } returns user
        every { userValidator.validateUserBeforeLogin(user) } just runs
        every { userCredentialsService.verifyCredential(user, PASSWORD) } just runs
        every { tokenService.generateAndSaveTokens(user) } returns expectedTokens

        // when
        val result = authService.login(EMAIL, PASSWORD)

        // then
        verify(exactly = 1) { userService.findUser(EMAIL) }
        verify(exactly = 1) { userValidator.validateUserBeforeLogin(user) }
        verify(exactly = 1) { userCredentialsService.verifyCredential(user, PASSWORD) }
        verify(exactly = 1) { tokenService.generateAndSaveTokens(user) }

        assertEquals(expectedTokens, result)
    }
}
