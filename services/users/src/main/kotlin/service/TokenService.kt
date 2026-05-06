package org.example.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.exception.TokenRefreshingException
import org.example.model.AuthTokens
import org.example.model.db_models.Token
import org.example.model.db_models.User
import org.example.model.db_models.enums.TokenType
import org.example.repository.TokenRepository
import org.example.service.security.JwtService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class TokenService(
    private val tokenRepository: TokenRepository,
    private val jwtService: JwtService,
) {

    private val logger = KotlinLogging.logger {}

    companion object {
        const val TOKEN_PREFIX = "Bearer "
    }

    fun getUserIdByToken(accessToken: String) = jwtService.extractUserId(accessToken)

    fun getPublicKey(): String {
        return jwtService.getPublicKey()
    }

    fun generateAndSaveTokens(user: User): AuthTokens {
        val access = jwtService.generateAccessToken(user)
        val now = LocalDateTime.now()

        tokenRepository.save(
            Token(
                user = user,
                tokenType = TokenType.ACCESS,
                tokenValue = jwtService.getTokenHash(access.token),
                issuedAt = now,
                expiresAt = access.expiresAt,
                revoked = false
            )
        )

        logger.info { "Saved access token for user ${user.username}" }

        val refresh = jwtService.generateRefreshToken()
        tokenRepository.save(
            Token(
                user = user,
                tokenType = TokenType.REFRESH,
                tokenValue = refresh.token,
                issuedAt = now,
                expiresAt = refresh.expiresAt,
                revoked = false
            )
        )

        logger.info { "Saved refresh token for user ${user.username}" }

        return AuthTokens(
            uuid = user.uid!!,
            accessToken = access.token,
            refreshToken = refresh.token
        )
    }

    fun refreshAccessToken(refreshToken: String): AuthTokens {
        val token = tokenRepository.findByTokenValueAndTokenType(refreshToken, TokenType.REFRESH)
            ?: throw TokenRefreshingException("Could not find refresh token")

        if (token.revoked || token.expiresAt.isBefore(LocalDateTime.now())) {
            logger.warn { "Refresh token expired" }
            throw TokenRefreshingException("Refresh token expired")
        }

        val user = token.user

        val activeAccessTokens = tokenRepository.findAllByUserAndTokenTypeAndRevokedFalse(
            user,
            TokenType.ACCESS
        )
        activeAccessTokens.forEach {
            it.revoked = true
            tokenRepository.save(it)
        }

        val newAccess = jwtService.generateAccessToken(user)

        tokenRepository.save(
            Token(
                user = user,
                tokenType = TokenType.ACCESS,
                tokenValue = jwtService.getTokenHash(newAccess.token),
                issuedAt = LocalDateTime.now(),
                expiresAt = newAccess.expiresAt,
                revoked = false
            )
        )

        return AuthTokens(
            uuid = user.uid!!,
            accessToken = newAccess.token,
            refreshToken = refreshToken
        )
    }
}