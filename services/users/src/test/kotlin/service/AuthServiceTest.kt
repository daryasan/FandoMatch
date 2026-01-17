package service

import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.service.AuthService
import org.example.service.TokenService
import org.example.service.UserCredentialsService
import org.example.service.UserService
import org.example.service.validation.UserValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.Constants.EMAIL
import utils.Constants.PASSWORD
import utils.Constants.PHONE
import utils.Constants.USERNAME
import utils.createUser

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

    @InjectMockKs
    private lateinit var authService: AuthService

    @Test
    fun `register should create user, save credentials, generate token`() {
        // given
        val user = createUser(EMAIL, PHONE, USERNAME)
        val expectedToken = "jwt-token-123"

        every { userService.createUser(EMAIL, PHONE, USERNAME) } returns user
        every { userCredentialsService.createCredentials(user, PASSWORD) } returns mockk()
        every { tokenService.generateAndSaveToken(user) } returns expectedToken

        // when
        val result = authService.register(EMAIL, PHONE, USERNAME, PASSWORD)

        // then
        verify(exactly = 1) { userService.createUser(EMAIL, PHONE, USERNAME) }
        verify(exactly = 1) { userCredentialsService.createCredentials(user, PASSWORD) }
        verify(exactly = 1) { tokenService.generateAndSaveToken(user) }

        assertEquals(expectedToken, result)
    }

    @Test
    fun `login should find user, validate, verify credentials and generate token`() {
        // given
        val user = createUser(EMAIL, PHONE, USERNAME)
        val expectedToken = "jwt-login-token"

        every { userService.findUser(EMAIL, PHONE, USERNAME) } returns user
        every { userValidator.validateUserBeforeLogin(user) } just runs
        every { userCredentialsService.verifyCredential(user, PASSWORD) } just runs
        every { tokenService.generateAndSaveToken(user) } returns expectedToken

        // when
        val result = authService.login(EMAIL, PHONE, USERNAME, PASSWORD)

        // then
        verify(exactly = 1) { userService.findUser(EMAIL, PHONE, USERNAME) }
        verify(exactly = 1) { userValidator.validateUserBeforeLogin(user) }
        verify(exactly = 1) { userCredentialsService.verifyCredential(user, PASSWORD) }
        verify(exactly = 1) { tokenService.generateAndSaveToken(user) }

        assertEquals(expectedToken, result)
    }
}
