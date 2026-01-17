package service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.example.model.GeneratedToken
import org.example.model.db_models.Token
import org.example.repository.TokenRepository
import org.example.service.TokenService
import org.example.service.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.Constants.EMAIL
import utils.Constants.PHONE
import utils.Constants.USERNAME
import utils.createUser
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class TokenServiceTest {

    @MockK
    lateinit var tokenRepository: TokenRepository

    @MockK
    lateinit var jwtService: JwtService

    @InjectMockKs
    private lateinit var tokenService: TokenService

    @Test
    fun `generateAndSaveToken should hash token and save Token entity`() {
        // given
        val user = createUser(EMAIL, PHONE, USERNAME)

        val rawToken = "raw.jwt.token"
        val expiresAt = LocalDateTime.now().plusHours(1)
        val generatedToken = GeneratedToken(token = rawToken, expiresAt = expiresAt)

        val expectedHash = "hashed_token_value"

        every { jwtService.generateToken(user) } returns generatedToken
        every { jwtService.getTokenHash(rawToken) } returns expectedHash
        every { tokenRepository.save(any()) } answers { firstArg() }

        val tokenSlot = slot<Token>()

        // when
        val returnedToken = tokenService.generateAndSaveToken(user)

        // then
        verify(exactly = 1) { jwtService.generateToken(user) }
        verify(exactly = 1) { jwtService.getTokenHash(rawToken) }
        verify(exactly = 1) { tokenRepository.save(capture(tokenSlot)) }

        val savedToken = tokenSlot.captured

        assertEquals(rawToken, returnedToken)
        assertEquals(user, savedToken.user)
        assertEquals(expectedHash, savedToken.tokenHash)
        assertEquals(expiresAt, savedToken.expiresAt)
        assertEquals(false, savedToken.revoked)
    }
}
