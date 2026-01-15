package service

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.example.model.GeneratedToken
import org.example.service.AuthService
import org.example.service.TokenService
import org.example.service.UserCredentialsService
import org.example.service.UserService
import org.example.service.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.createUser
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class AuthServiceTest {

    @MockK
    lateinit var userService: UserService
    @MockK
    lateinit var jwtService: JwtService
    @MockK
    lateinit var tokenService: TokenService
    @MockK
    lateinit var userCredentialsService: UserCredentialsService

    private lateinit var authService: AuthService

    @BeforeEach
    fun setup() {
        authService = AuthService(
            userService = userService,
            jwtService = jwtService,
            tokenService = tokenService,
            userCredentialsService = userCredentialsService
        )
    }

    @Test
    fun `register should create user, save credentials, generate token and save access token`() {
        // given
        val email = "test@example.com"
        val phone = "+123456789"
        val username = "testuser"
        val hashedPassword = "hashed_pass"

        val user = createUser(email, phone, username)
        val generatedToken = GeneratedToken(
            token = "jwt-token-123",
            expiresAt = LocalDateTime.now().plusHours(1)
        )

        every { userService.createUser(email, phone, username) } returns user
        every { userCredentialsService.createCredentials(user, hashedPassword) } returns mockk()
        every { jwtService.generateToken(user) } returns generatedToken
        every { tokenService.saveAccessToken(user, generatedToken) } returns Unit

        // when
        val result = authService.register(email, phone, username, hashedPassword)

        // then
        verify(exactly = 1) { userService.createUser(email, phone, username) }
        verify(exactly = 1) { userCredentialsService.createCredentials(user, hashedPassword) }
        verify(exactly = 1) { jwtService.generateToken(user) }
        verify(exactly = 1) { tokenService.saveAccessToken(user, generatedToken) }

        assertEquals("jwt-token-123", result)
    }
}
