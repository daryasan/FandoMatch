package service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.example.exception.TokenRefreshingException
import org.example.model.GeneratedToken
import org.example.model.db_models.Token
import org.example.model.db_models.enums.TokenType
import org.example.repository.TokenRepository
import org.example.service.TokenService
import org.example.service.security.JwtService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.Constants.EMAIL
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
    fun `generateAndSaveTokens should save access and refresh tokens`() {
        val user = createUser(EMAIL, USERNAME)

        val rawAccessToken = "raw.jwt.token"
        val accessExpires = LocalDateTime.now().plusHours(1)
        val generatedAccess = GeneratedToken(rawAccessToken, accessExpires)

        val expectedAccessHash = "hashed_access_token"

        val rawRefreshToken = "refresh-token-123"
        val refreshExpires = LocalDateTime.now().plusDays(60)
        val generatedRefresh = GeneratedToken(rawRefreshToken, refreshExpires)

        every { jwtService.generateAccessToken(user) } returns generatedAccess
        every { jwtService.getTokenHash(rawAccessToken) } returns expectedAccessHash
        every { jwtService.generateRefreshToken() } returns generatedRefresh
        every { tokenRepository.save(any()) } answers { firstArg() }

        val result = tokenService.generateAndSaveTokens(user)

        verify(exactly = 1) { jwtService.generateAccessToken(user) }
        verify(exactly = 1) { jwtService.getTokenHash(rawAccessToken) }
        verify(exactly = 1) { jwtService.generateRefreshToken() }
        verify(exactly = 2) { tokenRepository.save(any()) }

        val saved = mutableListOf<Token>()
        verify { tokenRepository.save(capture(saved)) }

        val access = saved[0]
        val refresh = saved[1]

        assertEquals(rawAccessToken, result.accessToken)
        assertEquals(rawRefreshToken, result.refreshToken)

        assertEquals(TokenType.ACCESS, access.tokenType)
        assertEquals(expectedAccessHash, access.tokenValue)
        assertEquals(user, access.user)
        assertEquals(accessExpires, access.expiresAt)

        assertEquals(TokenType.REFRESH, refresh.tokenType)
        assertEquals(rawRefreshToken, refresh.tokenValue)
        assertEquals(user, refresh.user)
        assertEquals(refreshExpires, refresh.expiresAt)
    }

    @Test
    fun `refreshAccessToken should revoke old access tokens and create new one`() {
        val user = createUser(EMAIL, USERNAME)

        val refreshTokenValue = "refresh-123"
        val refreshTokenEntity = Token(
            user = user,
            tokenType = TokenType.REFRESH,
            tokenValue = refreshTokenValue,
            issuedAt = LocalDateTime.now().minusDays(1),
            expiresAt = LocalDateTime.now().plusDays(1),
            revoked = false
        )

        val oldAccess1 = Token(
            user = user,
            tokenType = TokenType.ACCESS,
            tokenValue = "old-access-1",
            issuedAt = LocalDateTime.now().minusHours(2),
            expiresAt = LocalDateTime.now().plusHours(1),
            revoked = false
        )

        val oldAccess2 = Token(
            user = user,
            tokenType = TokenType.ACCESS,
            tokenValue = "old-access-2",
            issuedAt = LocalDateTime.now().minusHours(3),
            expiresAt = LocalDateTime.now().plusHours(2),
            revoked = false
        )

        every {
            tokenRepository.findByTokenValueAndTokenType(refreshTokenValue, TokenType.REFRESH)
        } returns refreshTokenEntity

        every {
            tokenRepository.findAllByUserAndTokenTypeAndRevokedFalse(user, TokenType.ACCESS)
        } returns listOf(oldAccess1, oldAccess2)

        val newAccessRaw = "new.access.token"
        val newAccessExpires = LocalDateTime.now().plusHours(1)
        val generatedAccess = GeneratedToken(newAccessRaw, newAccessExpires)

        every { jwtService.generateAccessToken(user) } returns generatedAccess
        every { jwtService.getTokenHash(newAccessRaw) } returns "hashed_new_access"
        every { tokenRepository.save(any()) } answers { firstArg() }

        val result = tokenService.refreshAccessToken(refreshTokenValue)

        verify(exactly = 1) {
            tokenRepository.findByTokenValueAndTokenType(refreshTokenValue, TokenType.REFRESH)
        }

        verify(exactly = 1) {
            tokenRepository.findAllByUserAndTokenTypeAndRevokedFalse(user, TokenType.ACCESS)
        }

        verify(exactly = 1) { jwtService.generateAccessToken(user) }
        verify(exactly = 1) { jwtService.getTokenHash(newAccessRaw) }

        val saved = mutableListOf<Token>()
        verify { tokenRepository.save(capture(saved)) }

        val revoked1 = saved[0]
        val revoked2 = saved[1]
        val newAccess = saved[2]

        assertEquals(true, revoked1.revoked)
        assertEquals(true, revoked2.revoked)

        assertEquals(TokenType.ACCESS, newAccess.tokenType)
        assertEquals("hashed_new_access", newAccess.tokenValue)
        assertEquals(user, newAccess.user)
        assertEquals(newAccessExpires, newAccess.expiresAt)
        assertEquals(false, newAccess.revoked)

        assertEquals(newAccessRaw, result.accessToken)
        assertEquals(refreshTokenValue, result.refreshToken)
    }

    @Test
    fun `refreshAccessToken should throw when refresh token not found`() {
        val refreshToken = "missing-refresh"

        every {
            tokenRepository.findByTokenValueAndTokenType(refreshToken, TokenType.REFRESH)
        } returns null

        assertThrows(TokenRefreshingException::class.java) {
            tokenService.refreshAccessToken(refreshToken)
        }

        verify(exactly = 1) {
            tokenRepository.findByTokenValueAndTokenType(refreshToken, TokenType.REFRESH)
        }

        verify(exactly = 0) { jwtService.generateAccessToken(any()) }
        verify(exactly = 0) { tokenRepository.save(any()) }
    }

    @Test
    fun `refreshAccessToken should throw when refresh token is revoked`() {
        val user = createUser(EMAIL, USERNAME)
        val refreshToken = "revoked-refresh"

        val refreshEntity = Token(
            user = user,
            tokenType = TokenType.REFRESH,
            tokenValue = refreshToken,
            issuedAt = LocalDateTime.now().minusDays(1),
            expiresAt = LocalDateTime.now().plusDays(1),
            revoked = true
        )

        every {
            tokenRepository.findByTokenValueAndTokenType(refreshToken, TokenType.REFRESH)
        } returns refreshEntity

        assertThrows(TokenRefreshingException::class.java) {
            tokenService.refreshAccessToken(refreshToken)
        }

        verify(exactly = 1) {
            tokenRepository.findByTokenValueAndTokenType(refreshToken, TokenType.REFRESH)
        }

        verify(exactly = 0) { tokenRepository.findAllByUserAndTokenTypeAndRevokedFalse(any(), any()) }
        verify(exactly = 0) { jwtService.generateAccessToken(any()) }
        verify(exactly = 0) { tokenRepository.save(any()) }
    }

    @Test
    fun `refreshAccessToken should throw when refresh token is expired`() {
        val user = createUser(EMAIL, USERNAME)
        val refreshToken = "expired-refresh"

        val refreshEntity = Token(
            user = user,
            tokenType = TokenType.REFRESH,
            tokenValue = refreshToken,
            issuedAt = LocalDateTime.now().minusDays(10),
            expiresAt = LocalDateTime.now().minusDays(1),
            revoked = false
        )

        every {
            tokenRepository.findByTokenValueAndTokenType(refreshToken, TokenType.REFRESH)
        } returns refreshEntity

        assertThrows(TokenRefreshingException::class.java) {
            tokenService.refreshAccessToken(refreshToken)
        }

        verify(exactly = 1) {
            tokenRepository.findByTokenValueAndTokenType(refreshToken, TokenType.REFRESH)
        }

        verify(exactly = 0) { tokenRepository.findAllByUserAndTokenTypeAndRevokedFalse(any(), any()) }
        verify(exactly = 0) { jwtService.generateAccessToken(any()) }
        verify(exactly = 0) { tokenRepository.save(any()) }
    }

}
